#!/bin/bash
# 格式: LOCAL_PORT:DESCRIPTION
TUNNELS=(
  "8091:Seata 服务"
  "7091:Seata 控制台"
)

for entry in "${TUNNELS[@]}"; do
  port="${entry%%:*}"
  desc="${entry#*:}"
  if lsof -i :"$port" -sTCP:LISTEN > /dev/null 2>&1; then
    echo "已连接  $port  $desc"
  else
    ssh -fNL "$port:127.0.0.1:$port" wydev && echo "已建立  $port  $desc"
  fi
done