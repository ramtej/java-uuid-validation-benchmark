package benchmarks;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.util.UUID;
import java.util.regex.Pattern;

@State(Scope.Benchmark)
public class RegexValidationCPUBoundBenchmark {

    // Pattern 1 was encountered in an actual application
    private static final Pattern UUID_PATTERN_1 = Pattern.compile("(:?[a-f0-9]){8,8}-(:?[a-f0-9]){4,4}-(:?[a-f0-9]){4,4}-(:?[a-f0-9]){4,4}-(:?[a-f0-9]){12,12}");

    // Patterns 2 - 4 are tuning steps (expected to get faster with each step)
    private static final Pattern UUID_PATTERN_2 = Pattern.compile("(:?[a-f0-9]){8}-(:?[a-f0-9]){4}-(:?[a-f0-9]){4}-(:?[a-f0-9]){4}-(:?[a-f0-9]){12}");
    private static final Pattern UUID_PATTERN_3 = Pattern.compile("[a-f0-9]{8}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{12}");
    private static final Pattern UUID_PATTERN_4 = Pattern.compile("^[a-f0-9]{8}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{12}$");

    // Pattern 5 takes into account that uppercase letters are valid for UUID.fromString()
    private static final Pattern UUID_PATTERN_5 = Pattern.compile("^[a-f0-9]{8}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{12}$", Pattern.CASE_INSENSITIVE);

    // Pattern 6 is taken from http://stackoverflow.com/a/13653180 and is more strict
    private static final Pattern UUID_PATTERN_6 = Pattern.compile("^[0-9a-f]{8}-[0-9a-f]{4}-[1-5][0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$", Pattern.CASE_INSENSITIVE);

    @Benchmark
    public Object baseLine() {
        return UUID.randomUUID().toString();
    }

    @Benchmark
    public Object baseLineInvalid() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    @Benchmark
    public Object measurePattern1() {
        return UUID_PATTERN_1.matcher(UUID.randomUUID().toString()).matches();
    }

    @Benchmark
    public Object measurePattern2() {
        return UUID_PATTERN_2.matcher(UUID.randomUUID().toString()).matches();
    }

    @Benchmark
    public Object measurePattern3() {
        return UUID_PATTERN_3.matcher(UUID.randomUUID().toString()).matches();
    }

    @Benchmark
    public Object measurePattern4() {
        return UUID_PATTERN_4.matcher(UUID.randomUUID().toString()).matches();
    }

    @Benchmark
    public Object measurePattern5() {
        return UUID_PATTERN_5.matcher(UUID.randomUUID().toString()).matches();
    }

    @Benchmark
    public Object measurePattern6() {
        return UUID_PATTERN_6.matcher(UUID.randomUUID().toString()).matches();
    }

    @Benchmark
    public Object measurePattern4WithLengthCheckValidLength() {
        final String value = UUID.randomUUID().toString();
        return value.length() == 36 && UUID_PATTERN_4.matcher(value).matches();
    }

    @Benchmark
    public Object measurePattern4WithLengthCheckInvalidLength() {
        // break the UUIDs somehow, for example by missing the dashes (common problem)
        final String value = UUID.randomUUID().toString().replace("-", "");
        return value.length() == 36 && UUID_PATTERN_4.matcher(value).matches();
    }

    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(".*" + RegexValidationCPUBoundBenchmark.class.getSimpleName() + ".*")
                .warmupIterations(5)
                .measurementIterations(10)
                .threads(4)
                .forks(2)
                .build();

        new Runner(opt).run();
    }
}
