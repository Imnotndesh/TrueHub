# TrueHub

TrueHub is an Android app for monitoring your **TrueNAS** system.  
It connects to the TrueNAS JSON-RPC API and shows system info, storage status, and performance stats.

## Features
- System overview (hostname, uptime, version)
- Storage pools and disks
- CPU and memory usage
- Quick actions (services, shutdown, refresh)

## Requirements
- Android 13.0 or higher
- A TrueNAS system (tested on 25.04)

## Build
```bash
git clone https://github.com/yourusername/truehub.git
cd truehub
./gradlew assembleDebug
