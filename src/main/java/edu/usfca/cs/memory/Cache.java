package edu.usfca.cs.memory;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by bingkunyang on 9/20/17.
 */
public abstract class Cache {
    public int CLIENT_SERVER_PORT = 5678;
    public int[] CLIENT_STORAGE_PORTS = {5650, 5651, 5652, 5653, 5654, 5655, 5656, 5657, 5658, 5659};
    public int[] SERVER_STORAGE_PORTS = {5660, 5661, 5662, 5663, 5664, 5665, 5666, 5667, 5668, 5669};

    public int SERVER_CLIENT_PORT = 6678;
    public int[] STORAGE_CLIENT_PORTS = {6650, 6651, 6652, 6653, 6654, 6655, 6656, 6657, 6658, 6659};
    public int[] STORAGE_SERVER_PORTS = {6660, 6661, 6662, 6663, 6664, 6665, 6666, 6667, 6668, 6669};

}
