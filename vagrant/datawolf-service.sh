[Unit]
Description=Datawolf Service
After=network.target

[Service]
User=incore
Group=users
Restart=on-failure
WorkingDirectory=/opt/datawolf
ExecStart=/opt/datawolf/bin/datawolf-service

[Install]
WantedBy=multi-user.target
