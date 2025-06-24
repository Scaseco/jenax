package jenax.engine.qlever.docker;

import java.util.stream.Collectors;
import java.util.stream.Stream;

public class QleverConstants {
    public static final String DOCKER_IMAGE_NAME = "adfreiburg/qlever";
    public static final String DOCKER_IMAGE_TAG = "commit-a307781";

    /** Returns the default image name if the argument is null. Otherwise the argument is returned. */
    public static String getImageName(String imageName) {
        String result = imageName == null
            ? DOCKER_IMAGE_NAME
            : imageName;
        return result;
    }

    /** Returns the default image tag if the argument is null. Otherwise the argument is returned. */
    public static String getImageTag(String imageTag) {
        String result = imageTag == null
            ? DOCKER_IMAGE_TAG
            : imageTag;
        return result;
    }

    public static String buildDockerImageName(String imageName, String imageTag) {
        String tag = getImageTag(imageTag);
        String image = getImageName(imageName);
        String result = Stream.of(image, tag)
            .filter(x -> x != null)
            .collect(Collectors.joining(":"));
        return result;
    }
}
