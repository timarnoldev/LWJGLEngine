package de.tim.renderengine.engine;

import de.tim.renderengine.models.RawModel;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;
import org.newdawn.slick.opengl.Texture;
import org.newdawn.slick.opengl.TextureLoader;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;

public class Loader {
    private final List<Integer> vaos = new ArrayList<Integer>();
    private final List<Integer> vbos = new ArrayList<Integer>();
    private final List<Integer> textures = new ArrayList<>();

    /**
     * Creates a VAO and Store it in the Video Mememory
     *
     * @param positions
     * @return
     */
    public RawModel loadToVAO(float[] positions ,float[] textureCoords, float[] normals,int[] indices) {
        int vaoID = createVAO();
        bindIndicesBuffer(indices);
        storeDataInAttributeList(0,3, positions);
        storeDataInAttributeList(1,2, textureCoords);
        storeDataInAttributeList(2,3, normals);
        unbindVAO();
        return new RawModel(vaoID, indices.length);
    }


    /***
     * Load Texture from The Res folder
     * @param fileName A png Texture
     * @return The ID of the Texture
     */

    public int loadTexture(String fileName) {
        Texture texture = null;
        try {
            texture = TextureLoader.getTexture("PNG",new FileInputStream("res/"+fileName+".png"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        int textureID = texture.getTextureID();
        textures.add(textureID);
        return textureID;
    }


    /**
     * Delete the existing VBOs and VAOs from the video memory
     */
    public void dispose() {
        for (int vao : vaos) {
            GL30.glDeleteVertexArrays(vao);
        }
        for (int vbo : vbos) {
            GL15.glDeleteBuffers(vbo);
        }

        for(int texture:textures) {
            GL11.glDeleteTextures(texture);
        }
    }

    /**
     * Creates a new VAO and returns its ID. A VAO holds geometry data that we
     * can render and is physically stored in memory on the GPU, so that it can
     * be accessed very quickly during rendering.
     * <p>
     * Like most objects in OpenGL, the new VAO is created using a "gen" method
     * which returns the ID of the new VAO. In order to use the VAO it needs to
     * be made the active VAO. Only one VAO can be active at a time. To make
     * this VAO the active VAO (so that we can store stuff in it) we have to
     * bind it.
     *
     * @return The ID of the newly created VAO.
     */
    private int createVAO() {
        int vaoID = GL30.glGenVertexArrays();
        vaos.add(vaoID);
        GL30.glBindVertexArray(vaoID);
        return vaoID;
    }

    /**
     * Stores the position data of the vertices into attribute 0 of the VAO. To
     * do this the positions must first be stored in a VBO. You can simply think
     * of a VBO as an array of data that is stored in memory on the GPU for easy
     * access during rendering.
     * <p>
     * Just like with the VAO, we create a new VBO using a "gen" method, and
     * make it the active VBO (so that we do stuff to it) by binding it.
     * <p>
     * We then store the positions data in the active VBO by using the
     * glBufferData method. We also indicate using GL_STATIC_DRAW that this data
     * won't need to be changed. If we wanted to edit the positions every frame
     * (perhaps to animate the quad) then we would use GL_DYNAMIC_DRAW instead.
     * <p>
     * We the connect the VBO to the VAO using the glVertexAttribPointer()
     * method. This needs to know the attribute number of the VAO where we want
     * to put the data, the number of floats used for each vertex (3 floats in
     * this case, because each vertex has a 3D position, an x, y, and z value),
     * the type of data (in this case we used floats) and then some other more
     * complicated stuff for storing the data in more fancy ways. Don't worry
     * about the last 3 parameters for now, we don't need them here.
     * <p>
     * Now that we've finished using the VBO we can unbind it. This isn't
     * totally necessary, but I think it's good practice to unbind the VBO when
     * you're done using it.
     *
     * @param attributeNumber - The number of the attribute of the VAO where the data is to
     *                        be stored.
     * @param data            - The geometry data to be stored in the VAO, in this case the
     *                        positions of the vertices.
     */
    private void storeDataInAttributeList(int attributeNumber,int coordinateSize, float[] data) {
        int vboID = GL15.glGenBuffers();
        vbos.add(vboID);
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vboID);
        FloatBuffer buffer = storeDataInFloatBuffer(data);
        GL15.glBufferData(GL15.GL_ARRAY_BUFFER, buffer, GL15.GL_STATIC_DRAW);
        GL20.glVertexAttribPointer(attributeNumber, coordinateSize, GL11.GL_FLOAT, false, 0, 0);
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
    }

    /**
     * Unbinds the VAO after we're finished using it. If we want to edit or use
     * the VAO we would have to bind it again first.
     */
    private void unbindVAO() {
        GL30.glBindVertexArray(0);
    }


    private void bindIndicesBuffer(int[] indices) {

        int vboID = GL15.glGenBuffers();
        vbos.add(vboID);
        GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, vboID);
        IntBuffer buffer = storeDatainIntBuffer(indices);
        GL15.glBufferData(GL15.GL_ELEMENT_ARRAY_BUFFER,buffer,GL15.GL_STATIC_DRAW);

    }

    private IntBuffer storeDatainIntBuffer(int[] data) {
        IntBuffer buffer = BufferUtils.createIntBuffer(data.length);
        buffer.put(data);
        buffer.flip();
        return buffer;
    }


    /**
     * Before we can store data in a VBO it needs to be in a certain format: in
     * a buffer. In this case we will use a float buffer because the data we
     * want to store is float data. If we were storing int data we would use an
     * IntBuffer.
     * <p>
     * First and empty buffer of the correct size is created. You can think of a
     * buffer as basically an array with a pointer. After putting the necessary
     * data into the buffer the pointer will have increased so that it points at
     * the first empty element of the array. This is so that we could add more
     * data to the buffer if we wanted and it wouldn't overwrite the data we've
     * already put in. However, we're done with storing data and we want to make
     * the buffer ready for reading. To do this we need to make the pointer
     * point to the start of the data, so that OpenGL knows where in the buffer
     * to start reading. The "flip()" method does just that, putting the pointer
     * back to the start of the buffer.
     *
     * @param data - The float data that is going to be stored in the buffer.
     * @return The FloatBuffer containing the data. This float buffer is ready
     * to be loaded into a VBO.
     */
    private FloatBuffer storeDataInFloatBuffer(float[] data) {
        FloatBuffer buffer = BufferUtils.createFloatBuffer(data.length);
        buffer.put(data);
        buffer.flip();
        return buffer;
    }

}
