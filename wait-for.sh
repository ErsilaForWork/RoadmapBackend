#!/usr/bin/env bash
# wait-for.sh
# Usage:
#   ./wait-for.sh host:port [--timeout seconds] [--interval seconds] -- command arg1 arg2 ...
# Example:
#   ./wait-for.sh roadmap_db:5432 --timeout 60 -- java -jar /opt/app/backend-app.jar
set -euo pipefail

if [ $# -lt 1 ]; then
  echo "Usage: $0 host:port [--timeout seconds] [--interval seconds] -- command"
  exit 2
fi

HOSTPORT="$1"
shift

# defaults
TIMEOUT=60
INTERVAL=1

# parse optional args
while [ $# -gt 0 ]; do
  case "$1" in
    --timeout)
      TIMEOUT="$2"
      shift 2
      ;;
    --interval)
      INTERVAL="$2"
      shift 2
      ;;
    --)
      shift
      break
      ;;
    *)
      # if we meet a token that is not an option assume it's the command
      break
      ;;
  esac
done

if [ $# -eq 0 ]; then
  echo "Error: no command supplied to run after wait." >&2
  exit 2
fi

CMD=( "$@" )

host="${HOSTPORT%%:*}"
port="${HOSTPORT##*:}"

if [ -z "$host" ] || [ -z "$port" ] || [ "$host" = "$port" ]; then
  echo "Invalid host:port -> '$HOSTPORT'" >&2
  exit 2
fi

log() {
  printf '%s %s\n' "$(date --iso-8601=seconds 2>/dev/null || date)" "$*"
}

end_time=$(( $(date +%s) + TIMEOUT ))
log "Waiting for $host:$port (timeout=${TIMEOUT}s, interval=${INTERVAL}s)..."

check_dev_tcp() {
  # bash /dev/tcp method
  (echo > /dev/tcp/"$host"/"$port") >/dev/null 2>&1
}

check_nc() {
  # nc -z supports different args on different nc implementations; try common ones
  if command -v nc >/dev/null 2>&1; then
    nc -z "$host" "$port" >/dev/null 2>&1 && return 0 || return 1
  else
    return 1
  fi
}

check_timeout_cmd() {
  # as a last resort, try timeout+bash connect if available
  if command -v timeout >/dev/null 2>&1; then
    timeout 1 bash -c "echo > /dev/tcp/'$host'/'$port'" >/dev/null 2>&1
    return $?
  fi
  return 1
}

while true; do
  # try checks in order: /dev/tcp (fast), nc, timeout fallback
  if check_dev_tcp 2>/dev/null; then
    log "$host:$port is available (via /dev/tcp)"
    break
  elif check_nc 2>/dev/null; then
    log "$host:$port is available (via nc)"
    break
  elif check_timeout_cmd 2>/dev/null; then
    log "$host:$port is available (via timeout/dev/tcp fallback)"
    break
  fi

  now=$(date +%s)
  if [ "$now" -ge "$end_time" ]; then
    log "Timeout after ${TIMEOUT}s waiting for $host:$port"
    exit 1
  fi

  sleep "$INTERVAL"
done

# exec the command (replaces shell with the command)
log "Starting: ${CMD[*]}"
exec "${CMD[@]}"
