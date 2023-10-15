package messageAgent.utilities;

public interface IOperation {

    String readAsync();

    void writeAsync(String message);
}
