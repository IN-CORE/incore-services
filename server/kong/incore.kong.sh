#add the apis that are forwarded by kong
curl -i -X DELETE --url http://localhost:8001/apis/fragility
curl -i -X POST \
  --url http://localhost:8001/apis/ \
  --data 'name=fragility' \
  --data 'uris=/fragility' \
  --data 'upstream_url=http://localhost:8888/fragility/'



curl -i -X DELETE --url http://localhost:8001/apis/maestro
curl -i -X POST \
  --url http://localhost:8001/apis/ \
  --data 'name=maestro' \
  --data 'uris=/maestro' \
  --data 'upstream_url=http://localhost:8888/maestro/'




curl -i -X DELETE --url http://localhost:8001/apis/repo
curl -i -X DELETE --url http://localhost:8001/apis/data
curl -i -X POST \
  --url http://localhost:8001/apis/ \
  --data 'name=datasets' \
  --data 'uris=/datasets' \
  --data 'upstream_url=http://localhost:8888/data/'




curl -i -X DELETE --url http://localhost:8001/apis/hazard
curl -i -X POST \
  --url http://localhost:8001/apis/ \
  --data 'name=hazard' \
  --data 'uris=/hazard' \
  --data 'upstream_url=http://localhost:8888/hazard/'




curl -i -X DELETE --url http://localhost:8001/apis/auth
curl -i -X POST \
  --url http://localhost:8001/apis/ \
  --data 'name=auth' \
  --data 'uris=/auth' \
  --data 'upstream_url=http://localhost:8888/auth/'




#create an anonyous user
curl -i -X POST \
  --url http://localhost:8001/consumers/ \
  --data 'username=anonymous' 

#get the anonymous user id
ANON_JSON=`curl -X GET  --url http://localhost:8001/consumers/anonymous`
fANON_ID=`echo "$ANON_JSON" | sed -e 's/.*"id":"\(.*\)"}/\1/g'`

#this requires that my ldaps patch has been applied to /usr/local/share/lua/xx/kong/
curl -i -X POST \
  --url http://localhost:8001/plugins/ \
  --data 'name=ldap-auth' \
  --data 'config.ldap_port=636' \
  --data 'config.base_dn=dc=ncsa,dc=illinois,dc=edu' \
  --data 'config.ldap_host=ldap.ncsa.illinois.edu' \
  --data 'config.attribute=uid' \
  --data 'config.ldaps=true' 



curl -i -X POST \
  --url http://localhost:8001/plugins/ \
  --data 'name=cors' \
  --data 'config.methods=GET,HEAD,PUT,PATCH,POST' \
  --data 'config.exposed_headers=X-Credential-User' \
  --data 'config.preflight_continue=false ' \
  --data 'config.origins=*' \
  --data 'config.headers=Access-Control-Allow-Headers,Accept,X-Requested-With,Content-Type,Access-Control-Request-Method,Access-Control-Request-Headers,Authorization,auth-user,auth-token,X-Credential-User' \




curl -i -X POST \
  --url http://localhost:8001/plugins/ \
  --data 'name=request-transformer' \
  --data "config.remove.headers=Access-Control-Allow-Origin" 

