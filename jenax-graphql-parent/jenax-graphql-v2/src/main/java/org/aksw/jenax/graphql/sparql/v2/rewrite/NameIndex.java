package org.aksw.jenax.graphql.sparql.v2.rewrite;

//public class NameIndex {
//
//    public static Field findOnlyField(Selection<?> start, String fieldName) {
//        Set<Field> matches = findField(start, fieldName);
//        // List<Path<String>> matchingPaths = matches.stream().map(Context::getPath).collect(Collectors.toList());
//
//        Field match;
//        if (matches.isEmpty()) {
//            throw new NoSuchElementException("Could not resolve field name " + fieldName); // + " at path " + this.getPath());
//        } else if (matches.size() > 1) {
//            throw new IllegalArgumentException("Ambiguous resolution. Field name + " + fieldName + " expected to resolve to 1 field. Got " + matches.size() + " fields"); // " + matchingPaths);
//        } else {
//            match = Iterables.getOnlyElement(matches);
//        }
//        return match;
//    }
//
//    public static Set<Field> findField(Selection<?> start, String name) {
//        Set<Field> result = Streams.stream(Traverser.<Node>forTree(x -> x.getChildren()).depthFirstPreOrder(start))
//            .filter(node -> node instanceof Field)
//            .map(node -> (Field)node)
//            .filter(field -> {
//                Set<String> names = getEffectiveFieldNames(field);
//                boolean r = names.contains(name);
//                return r;
//            })
//            .collect(Collectors.toSet());
//        return result;
//    }
//
//    public static Set<String> getEffectiveFieldNames(DirectivesContainer<?> container) {
//        Set<String> result = getAliases(container);
//        if (result.isEmpty()) {
//            if (container instanceof NamedNode<?> namedNode) {
//                result = Set.of(namedNode.getName());
//            }
//        }
//        return result;
//    }
//
//    public static Set<String> getAliases(DirectivesContainer<?> container) {
//        return streamAliases(container).collect(Collectors.toSet());
//    }
//
//    // Field has a getAlias() method - can we exploit that for our purpose???
//    // No, alias causes output fields to become renamed - we want to introduce a flat name by which a nested field can be referenced
//    public static Stream<String> streamAliases(DirectivesContainer<?> container) {
//        return container.getDirectives("as").stream()
//            .map(d -> GraphQlUtils.getArgValueAsString(d, "name", null)); // TODO Raise exception when field aliases are variables!
//    }
//}
