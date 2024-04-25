package org.aksw.jenax.io.out.config;

//
//public class RdfOutputConfigs {
//    private static final Logger logger = LoggerFactory.getLogger(RdfOutputConfigs.class);
//
//    public static StreamRDF openStream(RdfOutputConfig out, RDFFormat fallbackRdfFormat) {
//        PrefixMapping prefixes = new PrefixMappingTrie();
//
//        if (out.getPrefixSources() != null) {
//            for (String prefixSource : out.getPrefixSources()) {
//                logger.info("Adding prefixes from " + prefixSource);
//                Model tmp = RDFDataMgr.loadModel(prefixSource);
//                prefixes.setNsPrefixes(tmp);
//            }
//        }
//
//        String requestedFormat = out.getOutputFormat();
//
//        RDFFormat fmt = null;
//        // Try to derive the output format from the file name (if given)
//        if (requestedFormat != null) {
//            fmt = RDFLanguagesEx.findRdfFormat(requestedFormat);
//        }
//
//        FileNameParser fileNameParser = FileNameParser.of(
//                x -> ContentTypeUtils.getCtExtensions().getAlternatives().containsKey(x.toLowerCase()),
//                x -> ContentTypeUtils.getCodingExtensions().getAlternatives().containsKey(x.toLowerCase()));
//
//        String fileName = outputPath.getFileName().toString();
//
//        FileName fileInfo = fileNameParser.parse(fileName);
//        Function<OutputStream, OutputStream> encoder = RDFDataMgrEx.encoder(fileInfo.getEncodingParts());
//
//        if (fmt == null) {
//            String fileName = out.getTargetFile();
//            Lang lang = RDFDataMgr.determineLang(fileName, null, null);
//            if (lang != null) {
//                fmt = StreamRDFWriter.defaultSerialization(lang);
//            }
//        }
//
//        if (fmt == null) {
//            fmt = fallbackRdfFormat;
//        }
//
//        if (fmt == null) {
//            throw new RuntimeException("Could not determine an output RDF format.");
//        }
//
//        if (!StreamRDFWriter.registered(fmt)) {
//            throw new RuntimeException("No writer registered for RDF format " + fmt);
//        }
//
//        RDFFormat finalFmt = fmt;
//
//        Long deferPrefixCount = out.getPrefixOutputDeferCount();
//
//        return (os, cxt) -> {
//            StreamRDF writer = StreamRDFWriter.getWriterStream(os, finalFmt);
//            if (deferPrefixCount != null && deferPrefixCount > 0) {
//                writer = new StreamRDFDeferred(writer, true, prefixes, deferPrefixCount, 100 * deferPrefixCount, null);
//            }
//
//            return result;
//        };
//
////
////        result = result
////                .setGlobalPrefixMapping(prefixes)
////                .setMapQuadsToTriplesForTripleLangs(true)
////                .setDeferOutputForUsedPrefixes(out.getPrefixOutputDeferCount())
////                // .setAllowOverwriteFiles(true)
////                .setPartitionFolder(out.getPartitionFolder())
////                .setTargetFile(out.getTargetFile())
////                // .setUseElephas(true)
////                .setDeletePartitionFolderAfterMerge(true)
////                .setAllowOverwriteFiles(out.isOverwriteAllowed());
////                //.validate();
////
////        return result;
//    }
//}
