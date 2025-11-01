package ers.roadmap.DTO.patch;

import jakarta.validation.constraints.Size;

public class PatchGoalDTO {

    @Size(max = 2000, message = "Too long title")
    private String title;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
