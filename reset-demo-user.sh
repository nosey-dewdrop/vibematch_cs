#!/bin/bash
# removes ONE user (default su.bilge) and everything attached to them, so the
# live register->verify demo starts clean without an "already taken" error.
# the seed accounts (ada/mert/zeynep/can/elif) are left untouched.
#
# usage:  ./reset-demo-user.sh                 (clears su.bilge)
#         ./reset-demo-user.sh somebody         (clears that username)
#         ./reset-demo-user.sh su.bilge@ug.bilkent.edu.tr   (by email)

DB="vibematch.db"
TARGET="${1:-su.bilge}"

if [ ! -f "$DB" ]; then
  echo "no $DB yet -- nothing to clean (a fresh db has no su.bilge)."
  exit 0
fi

# resolve the username whether an email or a username was passed
UNAME=$(sqlite3 "$DB" "SELECT username FROM users WHERE username='$TARGET' OR email='$TARGET' LIMIT 1;")

if [ -z "$UNAME" ]; then
  echo "no account matching '$TARGET' in the db -- already clean."
  exit 0
fi

sqlite3 "$DB" <<SQL
DELETE FROM user_interests   WHERE username='$UNAME';
DELETE FROM memberships      WHERE username='$UNAME';
DELETE FROM spotify_profiles WHERE username='$UNAME';
DELETE FROM notifications    WHERE username='$UNAME';
DELETE FROM messages         WHERE sender='$UNAME' OR receiver='$UNAME';
DELETE FROM friendships      WHERE user_a='$UNAME' OR user_b='$UNAME';
DELETE FROM comments         WHERE author='$UNAME';
DELETE FROM posts            WHERE author='$UNAME';
DELETE FROM users            WHERE username='$UNAME';
SQL

echo "cleared '$UNAME' -- the register/verify demo can use that email fresh now."
echo "remaining users:"
sqlite3 "$DB" "SELECT username FROM users;"
