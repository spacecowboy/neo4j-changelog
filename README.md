# Neo4j-Changelog

A tool to generate changelogs based on GitHub pull requests. As far as
this tool is concerned, every entry in the changelog corresponds to
one PR on GitHub with a `changelog` tag.

The major feature of `neo4j-changelog` is the ability to deal with
parallel versions, where a change might occur in all major.minor
versions, or just subset of them.

## How it works

* Step 1

All pull requests labeled `changelog` are fetched from github.

* Step 2

All *relevant* tags are retrieved with git from a local clone of the
repo. A tag is considered relevant if-and-only-if it is a
[semantic version](http://semver.org/) *and* if it also has the same
major.minor version as the `--version` argument.

* Step 3a

Each PR is sorted under the earliest (by semantic version) tag from
which the PR's head commit is reachable. E.g., each PR is placed under
first tag which occurs after the relevant merge commit. If no such tag
exists, it is placed under the version specified by `--version`.

* Step 3b

The message of each PR is checked for a metadata tag, such as

    changelog: [2.3, packaging] This is a better message



### Specify metadata in PR message

### Examples

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

The examples all specify a version of `X.Y.99-NEXTVERSION` to remain
valid regardless of when you read this.


#### 3.1

```
neo4j-changelog \
  --from=$(git log upstream/3.0..upstream/3.1 --oneline | tail -n 1 | grep -o -e "^[0-9A-Za-z]\{7\}") \
  --to=upstream/3.1 \
  --version=3.1.99 \
  Kernel Cypher Packaging HA Core-Edge \
  "Import Tool" "Consistency Checker" \
  Metrics Server Shell Browser
```


#### 3.0

```
neo4j-changelog \
  --from=$(git log upstream/2.3..upstream/3.0 --oneline | tail -n 1 | grep -o -e "^[0-9A-Za-z]\{7\}") \
  --to=upstream/3.0 \
  --version=3.0.99 \
  Kernel Cypher Packaging HA Core-Edge \
  "Import Tool" "Consistency Checker" \
  Metrics Server Shell Browser
```



#### 2.3


```
neo4j-changelog \
  --from=$(git log upstream/2.2..upstream/2.3 --oneline | tail -n 1 | grep -o -e "^[0-9A-Za-z]\{7\}") \
  --to=upstream/2.3 \
  --version=2.3.99 \
  Kernel Cypher Packaging HA Core-Edge \
  "Import Tool" "Consistency Checker" \
  Metrics Server Shell Browser
```

#### 2.2

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
