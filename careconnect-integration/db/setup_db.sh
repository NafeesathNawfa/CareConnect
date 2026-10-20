#!/usr/bin/env bash
# Creates and seeds the free_clinic database. Run from the project root:  bash db/setup_db.sh
set -e
read -r -p "MySQL user [root]: " DBUSER
DBUSER=${DBUSER:-root}
for f in 01_schema 02_logic 03_seed 05_extensions; do
  echo "Loading db/$f.sql ..."
  mysql -u "$DBUSER" -p < "db/$f.sql"
done
echo "Done. Database free_clinic is ready."
