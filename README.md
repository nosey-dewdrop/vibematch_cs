# vibematch

A community matching app for Bilkent students. You pick your interests, take a short personality test, and the app shows you communities that actually fit you. You can join them, chat, post in the forum, and add friends.

## Tech stack

- Java 21, Swing (desktop UI)
- TCP socket client-server architecture (port 5050)
- SQLite (persistent database, sqlite-jdbc)
- Gson (JSON serialization, protocol layer)
- JavaMail + Gmail SMTP (email verification)
- MVC architecture: model / service / server / screens layers separated

## How it works

There is a server and a client. The server runs on one machine and holds the database. Everyone else connects to it over the network. When something happens (a message, a friend request) it gets pushed to you live, no refresh needed.

## Running it

Start the server first:
```
./run-server.sh
```

Then open the client in another terminal:
```
./run.sh
```

If you are connecting from a different laptop on the same wifi:
```
./run.sh <server ip address>
```

To find the server machine's ip, run this in the server's terminal:
```
ipconfig getifaddr en0
```

## Demo accounts

The database seeds itself on first run. You can log in with any of these:

| username | email | password |
|---|---|---|
| ada | ada@ug.bilkent.edu.tr | vibe1234 |
| mert | mert@ug.bilkent.edu.tr | vibe1234 |
| zeynep | zeynep@ug.bilkent.edu.tr | vibe1234 |
| can | can@ug.bilkent.edu.tr | vibe1234 |
| elif | elif@ug.bilkent.edu.tr | vibe1234 |

You can also sign up with your real Bilkent email. Only @ug.bilkent.edu.tr is accepted.

## Repo structure

```
model/        data classes (User, Community, Message ...)
data/         database layer, sqlite
server/       tcp server, one thread per client
net/          client side socket connection
service/      matching algorithm, mbti scoring, auth
screens/      all ui screens
protocol/     json format used over the socket
ui/           shared swing components and theme
model_cs/     original model classes from the team assignment
controller/   original controller classes from the team assignment
```

## Matching algorithm

It looks at two things: how many of the community's tags overlap with your interests (65% weight) and how close your personality type is to the typical member's type (35%). The result shows up as a match percent on each community card.

## Notes

- Data persists between runs. SQLite file: vibematch.db.
- Verification email is optional. If SMTP is not set up the code prints to the server console instead.
- To connect from another machine, the server and client need to be on the same network.
