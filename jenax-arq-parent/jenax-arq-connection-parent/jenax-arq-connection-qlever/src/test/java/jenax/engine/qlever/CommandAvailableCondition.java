package jenax.engine.qlever;

// ExecutionConditions require junit5 - we'd need to upgrade
//public class CommandAvailableCondition implements ExecutionCondition {
//    private static final String[] REQUIRED_COMMANDS = { "bash", "bzip2" };
//
//    @Override
//    public ConditionEvaluationResult evaluateExecutionCondition(ExtensionContext context) {
//        for (String command : REQUIRED_COMMANDS) {
//            if (!isCommandAvailable(command)) {
//                return ConditionEvaluationResult.disabled("Required command not found: " + command);
//            }
//        }
//        return ConditionEvaluationResult.enabled("All required commands are available.");
//    }
//
//    private boolean isCommandAvailable(String command) {
//        try {
//            Process process = new ProcessBuilder("which", command).start();
//            return process.waitFor() == 0;
//        } catch (IOException | InterruptedException e) {
//            return false;
//        }
//    }
//}
