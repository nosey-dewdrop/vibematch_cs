# what just got added and why

hey everyone -- damla here. big update before the demo, wanted to explain
what changed so you're not surprised.

## short version

the app now has a real server, a real database, and real socket connections.
two people on different laptops can sign up, log in, and use the app at the
same time. this is what we showed in the design report and what the demo needs.

## what i added (new packages, nothing you wrote was deleted)

**server/** -- a tcp server that runs on one machine. every client connects
to it over a socket. when something happens (a message, a friend request), it
pushes it live to whoever needs to see it. no polling.

**data/** -- sqlite database. everything persists now. accounts, communities,
memberships, messages, friends, notifications -- all saved to a file called
vibematch.db. restart the app and your account is still there.

**protocol/** -- the json format requests and responses use over the socket.
one json line per request, one json line per reply.

**net/** -- the client side of the connection. screens call Api.get().login()
or Api.get().homeMatches() instead of talking to a controller directly.

**service/** -- business logic (matching algorithm, mbti scoring, auth rules).
the server calls these.

**screens/** -- full ui connected to the real backend. login, register, email
verify, interest picker, vibe test, home feed with real match %, discover,
community detail, chats, notifications, friends, profile.

**ui/** -- shared swing helpers (theme colors, rounded panels, buttons).

## what happened to the old view/ and controller/ packages

they are still in the repo as view/ and model_cs/ -- i didn't delete anything.
the new screens/ replace the view/ panels for the demo because they are wired
to the real server. controller/ and model_cs/ are still there for reference
and for the report writeup.

## how to run it

terminal 1:
  ./run-server.sh

terminal 2 (same machine or different):
  ./run.sh
  (or ./run.sh 192.168.1.xx to connect to another machine on the same wifi)

the server creates vibematch.db on first run and seeds it with sample
communities automatically. you don't need to do anything.

## for the demo

one laptop runs ./run-server.sh. everyone else runs ./run.sh pointing at
that laptop's ip. you can sign up with your real bilkent email and actually
use the app together live.

damla
