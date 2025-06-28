
## Purpose

Build bash-like pipelines across different execution sites.

Bash Constructs:
* Conventional commands, such as `cat foo.bar` or `/bin/echo 'hi'`.
* Pipelines
* Groups

Execution Sites:
* Local host
* Docker containers
* JVM (commands mimicked in Java)

## Architecture

### Command Catalog
Registry of which commands *might* available on which execution sites.
Catalog entries are generally considered as candidates and as such are subject to resolution to make sure they actually exist on an execution site.


### Default resolutions
- how echo / cat / command -v and such get resolved.


### Command Shims

A command shim is a command line parser for another command implementation. It may only subset of the delegate's tools command line parameters.
The shim's purpose is to interpret a command line and tag any file arguments and whether they are used as input, output or both.


