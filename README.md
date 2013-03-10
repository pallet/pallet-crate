# pallet-crate

A Leiningen template for pallet crates.

## Usage

To create a new crate

    lein new pallet-crate some-crate

You can specify a group-id by namespacing the project name, and optionally
specify a different namespace prefix.

    lein new pallet-crate my.group/some-crate pallet.crate

## Conventions

The generated project is set up to be documented with the `lein-pallet-crate`
plugin.  The `crate-doc` task in that plugin will read `doc-src/USAGE.md` and
the meta data in `resources/pallet_crate/<your>_crate/meta.edn` to create
`README.md`.

## License

Copyright © 2013 Hugo Duncan

Distributed under the Eclipse Public License.
