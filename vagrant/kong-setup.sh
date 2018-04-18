#add the apis that are forwarded by kong
curl -i -X DELETE --url http://localhost:8001/apis/fragility
curl -i -X POST \
  --url http://localhost:8001/apis/ \
  --data 'name=fragility' \
  --data 'uris=/fragility' \
  --data 'upstream_url=http://10.0.2.2:8080/fragility/'



curl -i -X DELETE --url http://localhost:8001/apis/maestro
curl -i -X POST \
  --url http://localhost:8001/apis/ \
  --data 'name=maestro' \
  --data 'uris=/maestro' \
  --data 'upstream_url=http://10.0.2.2:8080/maestro/'




curl -i -X DELETE --url http://localhost:8001/apis/repo

curl -i -X POST \
  --url http://localhost:8001/apis/ \
  --data 'name=repo' \
  --data 'uris=/repo' \
  --data 'upstream_url=http://10.0.2.2:8080/repo/'





curl -i -X DELETE --url http://localhost:8001/apis/hazard

curl -i -X POST \
  --url http://localhost:8001/apis/ \
  --data 'name=hazard' \
  --data 'uris=/hazard' \
  --data 'upstream_url=http://10.0.2.2:8080/hazard/'


curl -i -X DELETE --url http://localhost:8001/apis/data
curl -i -X POST \
  --url http://localhost:8001/apis/ \
  --data 'name=data' \
  --data 'uris=/data' \
  --data 'upstream_url=http://10.0.2.2:8080/data/'

curl -i -X DELETE --url http://localhost:8001/apis/auth
curl -i -X POST \
  --url http://localhost:8001/apis/ \
  --data 'name=auth' \
  --data 'uris=/auth' \
  --data 'upstream_url=http://10.0.2.2:8080/auth/'


curl -i -X POST \
  --url http://localhost:8001/apis/ \
  --data 'name=datawolf' \
  --data 'uris=/datawolf' \
  --data 'upstream_url=http://localhost:8088/datawolf/'



#create an anonyous user
curl -i -X POST \
  --url http://localhost:8001/consumers/ \
  --data 'username=anonymous' 

#get the anonymous user id
ANON_JSON=`curl -X GET  --url http://localhost:8001/consumers/anonymous`
ANON_ID=`echo "$ANON_JSON" | sed -e 's/.*"id":"\(.*\)"}/\1/g'`

#this requires that my ldaps patch has been applied to /usr/local/share/lua/xx/kong/



curl -i -X DELETE --url http://localhost:8001/plugins/98c80b29-9aca-4124-bd45-e3195da2455a

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
  --data 'config.origins=*' \
  --data 'config.credentials=true' \
  --data 'config.headers=Access-Control-Allow-Headers,Origin,Accept,X-Requested-With,Content-Type,Access-Control-Request-Method,Access-Control-Request-Headers,Authorization,auth-user,auth-token,X-Credential-User' \
  --data 'config.exposed_headers=X-Credential-User' 





