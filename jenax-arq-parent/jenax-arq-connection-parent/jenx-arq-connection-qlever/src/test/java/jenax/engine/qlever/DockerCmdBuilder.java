package jenax.engine.qlever;

import java.util.List;

public class DockerCmdBuilder {
    public String imageName;
    private String workingDirectory;
    // public List<String> workingDirectory;
    
    public DockerCmdBuilder setImageName(String imageName) {
		this.imageName = imageName;
		return this;
	}
    
    public DockerCmdBuilder withWorkingDirectory(String workingDirectory) {
    	return this;
    }
    
    


    public String[] build() {
        return null;
    }
}
