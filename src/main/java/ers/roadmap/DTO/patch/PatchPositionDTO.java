package ers.roadmap.DTO.patch;

public class PatchPositionDTO {

    //Position of previous Action
    private Long prevId;

    //Position of next Action
    private Long nextId;

    public Long getPrevId() {
        return prevId;
    }

    public void setPrevId(Long prevId) {
        this.prevId = prevId;
    }

    public Long getNextId() {
        return nextId;
    }

    public void setNextId(Long nextId) {
        this.nextId = nextId;
    }
}
