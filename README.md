# Neo4j-Changelog

A tool to generate changelogs based on GitHub pull requests. As far as
this tool is concerned, every entry in the changelog corresponds to
one PR on GitHub with a `changelog` tag.

The major feature of `neo4j-changelog` is the ability to deal with
parallel versions, where a change might occur in all major.minor
versions, or just subset of them.


### How to use

Output of `neo4j-changelog --help`:

```
usage: neo4j-changelog [-h] [-ght GITHUBTOKEN] [-ghu GITHUBUSER] [-ghr GITHUBREPO] [-o OUTPUT]
                       [-d DIRECTORY] [-f FROM] -t TO -v VERSION [category [category ...]]

Generate changelog for the given project.

positional arguments:
  category               Categories  to  sort  changes   under.   These   should  match  (case-
                         insensitively) the tags of the GitHub  issues. Will always include the
                         catch-all category 'Misc'

optional arguments:
  -h, --help             show this help message and exit
  -ght GITHUBTOKEN, --githubtoken GITHUBTOKEN
                         GitHub Token (not required but heavily recommended) (default: )
  -ghu GITHUBUSER, --githubuser GITHUBUSER
                         Used to build the uri: github.com/user/repo (default: neo4j)
  -ghr GITHUBREPO, --githubrepo GITHUBREPO
                         Used to build the uri: github.com/user/repo (default: neo4j)
  -o OUTPUT, --output OUTPUT
                         Path to output file (default: CHANGELOG.md)
  -d DIRECTORY, --directory DIRECTORY
                         Path to local checked out git repo (default: ./)
  -f FROM, --from FROM   Gitref from which the  changelog  is  generated.  For  any  tags to be
                         included in  the  log,  this  commit  must  be  reachable  from  them.
                         (default: earliest commit in the log)
  -t TO, --to TO         Gitref up to which the  changelog  is  generated. Any tags included in
                         the log must be reachable from this commit.
  -v VERSION, --version VERSION
                         Latest/next semantic version. Any  changes  occurring after the latest
                         tag will be placed under this version in the log.
```

It is highly recommended to specify a github token for talking to the
Github API with. Otherwise you will be subject to heavy throttling,
daily limits, etc. You can get generate one at
[https://github.com/settings/tokens](https://github.com/settings/tokens).

`--from` should work with the default value, but it is highly
recommended to specify a value for it. You typically want a commit
which occurs before the first change in the changelog. The first
commit of a branch typically makes sense, which you can get with

```
git log branch1..branch2 --oneline | tail -n 1
```

which should print the first commit on branch2.

`--to` can be a specific version if you want to limit the changelog to
certain versions, but typically it will be the branch HEAD.

The examples all specify a version of `X.Y.99-NEXTVERSION` to remain
valid regardless of when you read this. It is important to note that
only tags with the same major.minor version as the specified version
will be listed.

The categories determine the subheaders which the changelog will
have. A non-exhaustive list from neo4j changelogs is:

```
Kernel Cypher Packaging HA Core-Edge "Import Tool" "Consistency Checker" Metrics Server Shell Browser
```

Some real examples follow (remember to specify your github token as well).

* 3.1

```
neo4j-changelog \
  --from=$(git log upstream/3.0..upstream/3.1 --oneline | tail -n 1 | grep -o -e "^[0-9A-Za-z]\{7\}") \
  --to=upstream/3.1 \
  --version=3.1.99 \
  Kernel Cypher Packaging HA Core-Edge \
  "Import Tool" "Consistency Checker" \
  Metrics Server Shell Browser
```


* 3.0

```
neo4j-changelog \
  --from=$(git log upstream/2.3..upstream/3.0 --oneline | tail -n 1 | grep -o -e "^[0-9A-Za-z]\{7\}") \
  --to=upstream/3.0 \
  --version=3.0.99 \
  Kernel Cypher Packaging HA Core-Edge \
  "Import Tool" "Consistency Checker" \
  Metrics Server Shell Browser
```


* 2.3


```
neo4j-changelog \
  --from=$(git log upstream/2.2..upstream/2.3 --oneline | tail -n 1 | grep -o -e "^[0-9A-Za-z]\{7\}") \
  --to=upstream/2.3 \
  --version=2.3.99 \
  Kernel Cypher Packaging HA Core-Edge \
  "Import Tool" "Consistency Checker" \
  Metrics Server Shell Browser
```

* 2.2

Due to the format of older changelogs, only the later part of 2.2's CHANGELOG can be auto generated.

```
neo4j-changelog \
  --from=2.2.7 \
  --to=upstream/2.2 \
  --version=2.2.99 \
  Kernel Cypher Packaging HA Core-Edge \
  "Import Tool" "Consistency Checker" \
  Metrics Server Shell Browser
```

### How it works

* Step 1

All pull requests labeled `changelog` are fetched from github.

* Step 2

All *relevant* tags are retrieved with git from a local clone of the
repo. A tag is considered relevant if-and-only-if it is a
[semantic version](http://semver.org/) *and* if it also has the same
major.minor version as the `--version` argument.

* Step 3

The message of each PR is checked for a tag, such as

    changelog: [2.3, packaging] This is a better message

which override the PR's GitHub labels, and the PR title. Versions
inside the brackets can be used to limit the inclusion of a change in
case it was null-forward-merged.

* Step 4

Each PR is sorted under the earliest (by semantic version) tag from
which the PR's head commit is reachable. E.g., each PR is placed under
first tag which occurs after the relevant merge commit. If no such tag
exists, it is placed under the version specified by `--version`. An
exception is if any versions were specified with the changelog tag in
the PR message. If so, the version must also share the same
major.minor version with at least one of the specified versions, else
the PR is not included in the log.

* Step 5

Each PR is now sorted under a category. Only categories given as
positional arguments to `neo4j-changelog` will be included, as well as
the default catch-all category `Misc`. The first GitHub label (or
label identified in step 3) to match one of the categories will be
used. If no match is found, it is placed under `Misc`.


### Specify metadata in PR message

By default, a PR's title is used as the changelog entry. The version
it is placed under depends solely on if the PRs head commit is
reachable from the tag representing the version (or the `--to`
ref). And the category it is sorted under is determined based on the
GitHub labels associated with the PR.

It is possible to override the metadata associated with a PR by adding
some text to the message body of the PR.

Some examples are the best way to illustrate.

A change should be placed under `packaging`, but the change is only
present on 2.2 and 2.3, e.g. it was a null merge when forward merged
to 3.0, and we want the changelog to state "Some fixes to Load CSV":

```
changelog: [2.2, 2.3, packaging] Some fixes to Load CSV
```

The colon after `changelog` is optional, and it is also OK to write
`cl` instead, so the following is perfectly equivalent:

```
cl [2.2, 2.3, packaging] Some fixes to Load CSV
```

In fact, we could also have placed it across several lines such as

```
changelog
[2.2, 2.3, packaging]
Some fixes to Load CSV
```

Each individual piece is of course optional, so if we are fine with
the title of PR being listed in the changelog we could just write:

```
CHANGELOG:[2.2,2.3,packaging]
```

The spacing and case don't matter. Similarly, if the PR is already tagged
correcly and it was not null-merged anywhere, but we are not OK with
the title, this would be fine:

```
cl: Some fixes to Load CSV
```

In case you were wondering, writing nothing will be ignored. These are
all equivalent, and they are effectively ignored when parsed:

```
changelog:
cl:
CHANGELOG
Cl
ChAngELog []
cL[]
```
