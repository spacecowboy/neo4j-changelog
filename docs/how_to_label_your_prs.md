
The primary purpose of the change log is to serve as the basis for the
release-notes. The secondary purpose is to allow interested users to
see what changed in a given version.

If you have a change which you think might be relevant for the change
log, this document explains how to get it included.

## Getting a PR into the changelog at all

The first step is to add the `changelog`-label on your PR on
Github. Only PRs with this label are downloaded and considered for
inclusion in the log.

![Add the changelog label to your PR](https://raw.githubusercontent.com/spacecowboy/neo4j-changelog/master/docs/AddChangelogLabel.png)

This creates an entry for the PR in the changelog as below:

```
#### Misc

- Add `import` to new AdminTool. [#7667](https://github.com/neo4j/neo4j/pull/7667)
```

## Set the category of the PR

As it stands, the PR is listed under *Misc* and obviously we can do
better than that. *Misc* is where anything not otherwise labeled ends
up. There are a fixed number of categories available (defined by the
config given to the tool). These categories are such as *Kernel*,
*Cypher*, *Security*, *Core Edge*, etc. If your PR is already labeled
with one of those, congratulations, you are already done!

This PR however is labeled with Operability, which is not one of the
categories in the change log. The suitable category in this case would
be *Tools*.

So just add the *Tools* label to the PR:

![Add the tools label to the PR](https://raw.githubusercontent.com/spacecowboy/neo4j-changelog/master/docs/AddToolsLabel.png)

Now the entry will move to the *Tools* section next time the tool runs:

```
#### Tools

- Add `import` to new AdminTool. [#7667](https://github.com/neo4j/neo4j/pull/7667)
```

## Set the changelog text

As you probably gathered, the text which is printed is just the PR
title. So just make your PR titles nice and descriptive:

![Change the title of the PR](https://raw.githubusercontent.com/spacecowboy/neo4j-changelog/master/docs/SetTitle.png)

* Caveat: The Github wiki does not automatically make things like
  `[#99]` into a link. If you want to link to an issue/pr, make it a
  proper markdown link:
  `[#99](https://github.com/neo4j/neo4j/issue/99)`

## Null Forward Merges

The tool handles forward merges perfectly, but it does need a little
help if a forward merge is a null-merge. E.g., a bug is fixed on 2.3,
but that piece of code (and bug) no longer exists on 3.0, so a
null-merge is performed between 2.3 and 3.0.

To give additional information to the tool, you can add a piece of
text to the PR description:

![Add meta data to PR](https://raw.githubusercontent.com/spacecowboy/neo4j-changelog/master/docs/AddMetaDeta.png)

To handle null forward merges, the only additional information
required is *which versions include this change*. So all that is
needed, is version(s) inside brackets:

```
changelog [2.2, 2.3]
```

The rest of the information is retrieved as before, via the PR title
and PR labels.

The tool looks for a line starting with *changelog* or *cl* (case
insensitive). Inside the brackets, versions and labels can be added.
The number of spaces do not matter.

## Meta-data reference

It is also possible to specify the title and label via the *changelog*
line in the description.

As before though, the PR must have the *changelog* label. This is
ALWAYS required.

The complete line is specified as follows:

```
changelog|cl [optional,VeRSIons,and,category] Optional overriding title
```

* The line must start with either *changelog* or *cl* (case doesn't matter)
* This is optionally followed by brackets `[ ]` containing a comma
  separated list of versions and/or category. See below.
* This is optionally followed by a String which override the PR title
  in the change log. (case DOES matter)

The comma separated list of versions and/or category

* the order of category and versions do not matter
* if version(s) are listed, the PR will ONLY show up in changelogs for
  those versions
* category is case-insensitive (matched to categories given in tool config)
* if no category is given, github labels are used
* if more than one valid category is listed, behavior is undefined

This means that I could have fixed the PR in the leading examples with
a description line like this:

```
changelog [tools] neo4j-admin: added import
```

But the recommended way is to rely on Github labels and proper PR
titles. Handling null forward-merges should specify the minimum
possible, e.g. only the additional version meta-data required.

One case when you must specify the category is when we for internal
reasons label a PR with for example *Kernel* and *Security*, but the
actual PR should be mentioned under *Procedures*. In that case, it is
appropriate to set the category manually:

```
changelog [procedures]
```

If your PR title can't be short and descriptive enough for the
changelog, you can override it. For example, if you need to link to
another issue/PR (remember, you must write the full link, Github will
NOT render `#99` as a link inside the Wiki):

```
changelog neo4j-admin: added import. See also [#99999](http://link/to/issue)
```
