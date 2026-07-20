#!/bin/sh
set -eu

find_shared_dir() {
  dir="$1"
  while [ "$dir" != "/" ]; do
    if [ -f "$dir/AppRecommendations/RecommendedApps.json" ]; then
      printf '%s/AppRecommendations\n' "$dir"
      return 0
    fi
    dir="$(dirname "$dir")"
  done
  return 1
}

search_root="${SRCROOT:-${PROJECT_DIR:-$(pwd)}}"
shared_dir="$(find_shared_dir "$search_root")"
resources_dir="${TARGET_BUILD_DIR}/${UNLOCALIZED_RESOURCES_FOLDER_PATH}"

mkdir -p "$resources_dir"
cp "$shared_dir/RecommendedApps.json" "$resources_dir/RecommendedApps.json"

for icon in "$shared_dir"/Icons/*.png; do
  [ -e "$icon" ] || continue
  cp "$icon" "$resources_dir/$(basename "$icon")"
done
