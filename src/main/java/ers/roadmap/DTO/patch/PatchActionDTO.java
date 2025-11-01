package ers.roadmap.DTO.patch;


import jakarta.validation.constraints.Size;

public class PatchActionDTO {

    @Size(max = 2000, message = "Too long title")
    private String title;

    @Size(max = 3000, message = "Too long description")
    private String description;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
