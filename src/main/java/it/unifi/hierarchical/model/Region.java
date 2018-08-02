package it.unifi.hierarchical.model;

public class Region {
    
    public enum RegionType{
        FINAL,
        EXIT
    }
    
    private State initialState;
    private RegionType type;

    public Region(State initialState, RegionType type) {
        this.initialState = initialState;
        this.type = type;
    }

    public State getInitialState() {
        return initialState;
    }

    public RegionType getType() {
        return type;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((initialState == null) ? 0 : initialState.hashCode());
        result = prime * result + ((type == null) ? 0 : type.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Region other = (Region) obj;
        if (initialState == null) {
            if (other.initialState != null)
                return false;
        } else if (!initialState.equals(other.initialState))
            return false;
        if (type != other.type)
            return false;
        return true;
    }
    
    
   
}
