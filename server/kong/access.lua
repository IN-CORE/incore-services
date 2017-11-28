-- /usr/local/share/lua/5.1/kong/plugins/ldap-auth/access.lua

local responses = require "kong.tools.responses"
local constants = require "kong.constants"
local singletons = require "kong.singletons"
local ldap = require "kong.plugins.ldap-auth.ldap"

local match = string.match
local ngx_log = ngx.log
local request = ngx.req
local gettime = ngx.now
local ngx_error = ngx.ERR
local ngx_debug = ngx.DEBUG
local decode_base64 = ngx.decode_base64
local ngx_socket_tcp = ngx.socket.tcp
local ngx_set_header = ngx.req.set_header
local tostring =  tostring

local AUTHORIZATION = "authorization"
local PROXY_AUTHORIZATION = "proxy-authorization"

local _M = {}

local function retrieve_credentials(authorization_header_value)
  local username, password
  if authorization_header_value then
    local cred = match(authorization_header_value, "%s*[ldap|LDAP]%s+(.*)")

    if cred ~= nil then
      local decoded_cred = decode_base64(cred)
      username, password = match(decoded_cred, "(.+):(.+)")
    end
  end
  return username, password
end

local function ldap_authenticate(given_username, given_password, conf)
  local is_authenticated
  local err, suppressed_err, ok
  local who = conf.attribute.."="..given_username..","..conf.base_dn

  local sock = ngx_socket_tcp()
  sock:settimeout(conf.timeout)
  ok, err = sock:connect(conf.ldap_host, conf.ldap_port)
  if not ok then
    ngx_log(ngx_error, "[ldap-auth] failed to connect to "..conf.ldap_host..":"..tostring(conf.ldap_port)..": ", err)
    return nil, err, responses.status_codes.HTTP_INTERNAL_SERVER_ERROR
  end

  if conf.start_tls then
    local success, err = ldap.start_tls(sock)
    if not success then
      return false, err
    end
    local _, err = sock:sslhandshake(true, conf.ldap_host, conf.verify_ldap_host)
    if err ~= nil then
      return false, "failed to do SSL handshake with "..conf.ldap_host..":"..tostring(conf.ldap_port)..": ".. err
    end
  end

  if conf.ldaps then
    local _, err = sock:sslhandshake(true, conf.ldap_host, conf.verify_ldap_host)
    if err ~= nil then
      return false, "failed to do SSL handshake with "..conf.ldap_host..":"..tostring(conf.ldap_port)..": ".. err
    end
  end


  is_authenticated, err = ldap.bind_request(sock, who, given_password)

  ok, suppressed_err = sock:setkeepalive(conf.keepalive)
  if not ok then
    ngx_log(ngx_error, "[ldap-auth] failed to keepalive to "..conf.ldap_host..":"..tostring(conf.ldap_port)..": ", suppressed_err)
  end
  return is_authenticated, err
end

local function load_credential(given_username, given_password, conf)
  ngx_log(ngx_debug, "[ldap-auth] authenticating user against LDAP server: "..conf.ldap_host..":"..conf.ldap_port)

  local ok, err, status = ldap_authenticate(given_username, given_password, conf)
  if status ~= nil then return nil, err, status end
  if err ~= nil then ngx_log(ngx_error, err) end
  if not ok then
    return nil
  end
  return {username = given_username, password = given_password}
end

local function return_token(token) 
  return token
end

-- NLT adding token
local function authenticate(conf, given_credentials, auth_user, auth_token)


  ngx_log(ngx_debug, "[ldap-auth] attempting authenticate ")
  -- if they passed in an auth token, see if it matches the cached token
  if auth_user ~= nil then
    ngx_log(ngx_debug, "[ldap-auth] auth_user is:"..auth_user)
    ngx_log(ngx_debug, "[ldap-auth] auth_token is:'"..auth_token.."'")
    local cache_key = "ldap_auth_cache:" .. ngx.ctx.api.id .. ":" .. given_username
    local cached_token = singletons.cache:get(cache_key)

    if cached_token ~= nil then
      ngx_log(ngx_debug, "[ldap-auth] cached token is:'"..cached_token.."'")
      if tostring(cached_token) == tostring(auth_token) then
        ngx_log(ngx_debug, "[ldap-auth] CACHE MATCH!")
        return true, {username = auth_user, password = auth_token}, auth_token
      else
      ngx_log(ngx_debug, "[ldap-auth] c'"..cached_token.."'")
      ngx_log(ngx_debug, "[ldap-auth] t'"..auth_token.."'")
      ngx_log(ngx_debug, "[ldap-auth] c'"..type(cached_token).."'")
      ngx_log(ngx_debug, "[ldap-auth] t'"..type(auth_token).."'")
      end
    else
      ngx_log(ngx_debug, "[ldap-auth] cached token is nil")
    end
  end

  -- if they passed in an auth token and it doesn't match, or there's no auth token, attempt ldap password auth
  -- parse out username, password from weird format
  local given_username, given_password = retrieve_credentials(given_credentials)
  if given_username ~= nil then
    ngx_log(ngx_debug, "[ldap-auth] decoding username/password for"..given_username)
  else
    ngx_log(ngx_debug, "[ldap-auth] decoding username/password for nil username")
  end


  -- if no username, fail
  if given_username == nil then
    ngx_log(ngx_debug, "[ldap-auth] fail, no username")
    return false
  end



  -- attempt to connect to ldap
  local ldap_response, err, status = load_credential(given_username, given_password, conf)
  -- if failed on ldap auth, return false
  if ldap_response == nil then
    ngx_log(ngx_debug, "[ldap-auth] fail, no ldap response")
    return false
  end

  -- if succeeded, generate and cache a token
  local new_token = tostring(math.random(1, 99999999999) + gettime() * 1000) .. tostring(math.random(1,99999999))
  ngx_log(ngx_debug, "[ldap-auth] success, new token is:"..new_token)
  local cache_key = "ldap_auth_cache:" .. ngx.ctx.api.id .. ":" .. given_username
  singletons.cache:get(cache_key, {ttl=10000, neg_ttl = conf.cache_ttl}, return_token, new_token)

  return true, {username = given_username, password = auth_token}, new_token
end



local function load_consumer(consumer_id, anonymous)
  local result, err = singletons.dao.consumers:find { id = consumer_id }
  if not result then
    if anonymous and not err then
      err = 'anonymous consumer "'..consumer_id..'" not found'
    end
    return nil, err
  end
  return result
end

--NLT add token
local function set_consumer(consumer, credential, token)
  
  if consumer then
    -- this can only be the Anonymous user in this case
    ngx_set_header(constants.HEADERS.CONSUMER_ID, consumer.id)
    ngx_set_header(constants.HEADERS.CONSUMER_CUSTOM_ID, consumer.custom_id)
    ngx_set_header(constants.HEADERS.CONSUMER_USERNAME, consumer.username)
    ngx_set_header(constants.HEADERS.ANONYMOUS, true)
    ngx.ctx.authenticated_consumer = consumer
    return
  end
  
  -- here we have been authenticated by ldap
  ngx_set_header(constants.HEADERS.CREDENTIAL_USERNAME, credential.username)
  ngx.ctx.authenticated_credential = credential

  -- NLT
  ngx_set_header("auth_token", token)
  
  -- in case of auth plugins concatenation, remove remnants of anonymous
  ngx.ctx.authenticated_consumer = nil
  ngx_set_header(constants.HEADERS.ANONYMOUS, nil)
  ngx_set_header(constants.HEADERS.CONSUMER_ID, nil)
  ngx_set_header(constants.HEADERS.CONSUMER_CUSTOM_ID, nil)
  ngx_set_header(constants.HEADERS.CONSUMER_USERNAME, nil)

end

local function do_authentication(conf)
  local headers = request.get_headers()
  local authorization_value = headers[AUTHORIZATION]
  local proxy_authorization_value = headers[PROXY_AUTHORIZATION]
  local auth_token = headers["auth_token"]
  local auth_user = headers["auth_user"]

  -- If both headers are missing, return 401
  if not (authorization_value or proxy_authorization_value) then
    ngx.header["WWW-Authenticate"] = 'LDAP realm="kong"'
    return false, {status = 401}
  end

  -- NLT adding auth token
  local is_authorized, credential, token = authenticate(conf, proxy_authorization_value, auth_user, auth_token)
  if not is_authorized then
    is_authorized, credential, token = authenticate(conf, authorization_value, auth_user, auth_token)
  end

  if not is_authorized then
    return false, {status = 403, message = "Invalid authentication credentials"}
  end

  if conf.hide_credentials then
    request.clear_header(AUTHORIZATION)
    request.clear_header(PROXY_AUTHORIZATION)
  end

  -- NLT add token
  set_consumer(nil, credential, token)

  return true
end


function _M.execute(conf)

  if ngx.ctx.authenticated_credential and conf.anonymous ~= "" then
    -- we're already authenticated, and we're configured for using anonymous, 
    -- hence we're in a logical OR between auth methods and we're already done.
    return
  end

  local ok, err = do_authentication(conf)
  if not ok then
    if conf.anonymous ~= "" and conf.anonymous ~= nil then
      -- get anonymous user
      local cache_key = "ldap_auth_cache:" .. ngx.ctx.api.id .. ":" .. conf.anonymous
      local consumer, err = singletons.cache:get(cache_key,
                       nil, load_consumer, conf.anonymous, true)
      if err then
        responses.send_HTTP_INTERNAL_SERVER_ERROR(err)
      end
      -- NLT add token
      set_consumer(consumer, nil, nil)
    else
      return responses.send(err.status, err.message)
    end
  end
end


return _M

