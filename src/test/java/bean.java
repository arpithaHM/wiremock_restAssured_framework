public class bean {

    private String id;
    private String location;
    private String cost;
    private String fileName;

    public String getInvalidLocationName() {
        return invalidLocationName;
    }

    public void setInvalidLocationName(String invalidLocationName) {
        this.invalidLocationName = invalidLocationName;
    }

    private String invalidLocationName;

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public  bean(){

    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getCost() {
        return cost;
    }

    public void setCost(String cost) {
        this.cost = cost;
    }

}
