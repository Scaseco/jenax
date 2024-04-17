# jenax-models-polyfill

Models for specifying vendor-specific sequences of sparql query transforms.

## Not used
This module is in design phase.
Its not yet clear to what extent an RDF vocubulary for custom query rewriters / optimizers is needed.
The main use case would be customizability with third party plugins.

However, in general it seems more worthwhile to auto-detect limitations of triple stores.
Auto-detectors could be registered using plain java SPI plugins.
