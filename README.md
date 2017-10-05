# Protocol Buffers Example

This extends the Project 1 template code with a simple server and client that send messages via Protocol Buffers.

To work with Protocol Buffers on your own machine, you'll need to install the ```protoc``` compiler. Install via your package manager:

* **protobuf** on homebrew 
* **protobuf-compiler** on RedHat-based distributions

...or download via the [Protocol Buffers Homepage](https://developers.google.com/protocol-buffers/)


The following is my assumption:
1. The write for the 3 chunks will have at least one write successful;
2. Not all DataNode are down at the same time(at least one is sending for 15sec);


Some of the specification of this project:
1. Store project under folder home4 and store data in the folder home2;
