package jenax.engine.qlever.docker;

import java.util.List;
import java.util.Objects;
import java.util.function.Function;

public class QleverCliUtils {

    public static <T, X> X get(T a, T b, Function<? super T, X> getter, X fallback) {
        X result = get(a, b, getter);
        if (result == null) {
            result = fallback;
        }
        return result;
    }

    public static <T, X> X get(T a, T b, Function<? super T, X> getter) {
        X result = getter.apply(a);
        if (result == null && b != null) {
            result = getter.apply(b);
        }
        return result;
    }

    public static void accumulateCliOptions(List<String> args, QleverConfRun cnf) {
        Objects.requireNonNull(args);
        Objects.requireNonNull(cnf);

        String s;
        Boolean b;
        Integer i;
        Long l;

        if ((s = cnf.getIndexBaseName()) != null) {
            args.add("-i");
            args.add(s);
        }
        if((i = cnf.getPort()) != null) {
            args.add("-p");
            args.add(Integer.toString(i));
        }
        if((s = cnf.getAccessToken()) != null) {
            args.add("-a");
            args.add(s);
        }
        if((i = cnf.getNumSimultaneousQueries()) != null) {
            args.add("-j");
            args.add(Integer.toString(i));
        }
        if((s = cnf.getMemoryMaxSize()) != null) {
            args.add("-m");
            args.add(s);
        }
        if((s = cnf.getCacheMaxSize()) != null) {
            args.add("-c");
            args.add(s);
        }
        if((s = cnf.getCacheMaxSizeSingleEntry()) != null) {
            args.add("-e");
            args.add(s);
        }
        if((s = cnf.getLazyResultMaxCacheSize()) != null) {
            args.add("-E");
            args.add(s);
        }
        if((l = cnf.getCacheMaxNumEntries()) != null) {
            args.add("-k");
            args.add(Long.toString(l));
        }
        if((b = cnf.getNoPatterns()) != null && b) {
            args.add("-P");
        }
        if((b = cnf.getNoPatternTrick()) != null && b) {
            args.add("-T");
        }
        if((b = cnf.getText()) != null && b) {
            args.add("-t");
        }
        // XXX --only-pso-and-pos-permutations
        if((s = cnf.getDefaultQueryTimeout()) != null) {
            args.add("-s");
            args.add(s);
        }
        if((l = cnf.getServiceMaxValueRows()) != null) {
            args.add("-S");
            args.add(Long.toString(l));
        }
        if((b = cnf.getThrowOnUnboundVariables()) != null && b) {
            args.add("--throw-on-unbound-variables");
            args.add("true");
        }
    }
}
