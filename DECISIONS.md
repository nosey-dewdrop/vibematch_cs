# decisions

one line per non obvious call: decision / why / cost to undo.

- spotify oauth runs on the CLIENT, not the server / the desktop app opens a
  browser and catches the loopback redirect, the server is headless and cant /
  low: it's isolated in net/spotify, server only stores the result

- spotify uses authorization code + PKCE, no client secret / a swing app cant
  keep a secret safe, and spotify allows PKCE for desktop apps / low

- spotify data lives in its own table spotify_profiles, not columns on users /
  keeps it separate, disconnect is one delete, no ALTER TABLE on old dbs / low

- top genres from spotify are added to the user's interests so they feed the
  existing match algorithm / that's the whole point ("match by music taste") /
  low: they're tagged and can be stripped back out on disconnect
