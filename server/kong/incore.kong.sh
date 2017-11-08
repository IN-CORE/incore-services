#add the apis that are forwarded by kong
curl -i -X POST \
  --url http://localhost:8001/apis/ \
  --data 'name=fragility' \
  --data 'uris=/fragility' \
  --data 'upstream_url=http://localhost:8080/fragility/'

curl -i -X POST \
  --url http://localhost:8001/apis/ \
  --data 'name=maestro' \
  --data 'uris=/maestro' \
  --data 'upstream_url=http://localhost:8080/maestro/'

curl -i -X POST \
  --url http://localhost:8001/apis/ \
  --data 'name=repo' \
  --data 'uris=/repo' \
  --data 'upstream_url=http://localhost:8080/repo/'

curl -i -X POST \
  --url http://localhost:8001/apis/ \
  --data 'name=hazard' \
  --data 'uris=/hazard' \
  --data 'upstream_url=http://localhost:8080/hazard/'


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
  --data 'config.anonymous=XXX' \
  --data 'config.base_dn=dc=ncsa,dc=illinois,dc=edu' \
  --data 'config.ldap_host=ldap.ncsa.illinois.edu' \
  --data 'config.attribute=uid' \
  --data 'config.ldaps=true' \
  --data "config.anonymous=$ANON_ID" 
  
