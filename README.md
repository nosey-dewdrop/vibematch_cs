# vibematch

a community matching app for bilkent students. you pick your interests, take a short personality test, and the app shows you communities that actually fit you. you can join them, chat, post in the forum, and add friends.

## how it works

there's a server and a client. the server runs on one machine and holds the database. everyone else connects to it over the network. when something happens (a message, a friend request) it gets pushed to you live, you don't have to refresh anything.

## running it

start the server first:
```
./run-server.sh
```

then open the client in another terminal:
```
./run.sh
```

if you're on a different laptop on the same wifi:
```
./run.sh <server machine's ip>
```

to find the ip of the server machine: `ipconfig getifaddr en0`

## demo accounts

the database seeds itself on first run. you can log in with any of these:

| username | email | password |
|---|---|---|
| ada | ada@ug.bilkent.edu.tr | vibe1234 |
| mert | mert@ug.bilkent.edu.tr | vibe1234 |
| zeynep | zeynep@ug.bilkent.edu.tr | vibe1234 |
| can | can@ug.bilkent.edu.tr | vibe1234 |
| elif | elif@ug.bilkent.edu.tr | vibe1234 |

or sign up with your real bilkent email (it only accepts @ug.bilkent.edu.tr).

## what's in the repo

```
model/        the data classes (User, Community, Message, ...)
data/         database layer, sqlite
server/       the tcp server, one thread per client
net/          client side socket connection
service/      matching algorithm, mbti scoring, auth
screens/      all the ui screens
protocol/     the json format requests use over the socket
ui/           shared swing components and theme
model_cs/     the original model classes from the team assignment
controller/   the original controller classes from the team assignment
```

## the matching algorithm

it looks at two things: how many of the community's tags overlap with your interests (65% weight) and how close your personality type is to the typical member's type (35%). the score shows up as a match percent on each community card.

## notes

- accounts and data persist between runs (sqlite file: vibematch.db)
- the verification email is optional, if smtp isn't set up the code prints to the server console
- to connect from another machine the server and client need to be on the same network
