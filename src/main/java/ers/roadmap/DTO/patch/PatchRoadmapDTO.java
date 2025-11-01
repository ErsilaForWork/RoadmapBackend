package ers.roadmap.DTO.patch;

import jakarta.validation.constraints.Size;

public class PatchRoadmapDTO {

    @Size(max = 2000, message = "Too long title for roadmap!")
    private String title;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
