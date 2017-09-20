curl -i -X POST \
  --url http://localhost:8001/fragility/api/ \
  --data 'name=fragility' \
  --data 'hosts=example.com' \
  --data 'upstream_url=http://httpbin.org'
