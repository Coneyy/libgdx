package coneys.com.github.libtest

import com.badlogic.gdx.ApplicationListener
import com.badlogic.gdx.Files
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.PerspectiveCamera
import com.badlogic.gdx.graphics.g3d.Environment
import com.badlogic.gdx.graphics.g3d.Model
import com.badlogic.gdx.graphics.g3d.ModelBatch
import com.badlogic.gdx.graphics.g3d.ModelInstance
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute
import com.badlogic.gdx.graphics.g3d.loader.G3dModelLoader
import com.badlogic.gdx.graphics.g3d.utils.AnimationController
import com.badlogic.gdx.graphics.g3d.utils.CameraInputController
import com.badlogic.gdx.utils.UBJsonReader

import javax.microedition.khronos.opengles.GL10


class ModelTest : ApplicationListener {


    private var camera: PerspectiveCamera? = null
    private var modelBatch: ModelBatch? = null
    private var model: Model? = null
    private var modelInstance: ModelInstance? = null
    private var environment: Environment? = null
    private var controller: AnimationController? = null

    override fun create() {
        // Create camera sized to screens width/height with Field of View of 75 degrees
        camera = PerspectiveCamera(
                75f,
                Gdx.graphics.width.toFloat(),
                Gdx.graphics.height.toFloat())

        // Move the camera 5 units back along the z-axis and look at the origin
        camera!!.position.set(0f, 0f, 10f)
        camera!!.lookAt(0f, 0f, 0f)

        // Near and Far (plane) represent the minimum and maximum ranges of the camera in, um, units
        camera!!.near = 0.1f
        camera!!.far = 300.0f

        val camController = CameraInputController(camera)
        camController.pinchZoomFactor = 0f

        Gdx.input.inputProcessor = camController

        // A ModelBatch is like a SpriteBatch, just for models.  Use it to batch up geometry for OpenGL
        modelBatch = ModelBatch()

        // Model loader needs a binary json reader to decode
        val jsonReader = UBJsonReader()
        // Create a model loader passing in our json reader
        val modelLoader = G3dModelLoader(jsonReader)
        // Now load the model by name
        // Note, the model (g3db file ) and textures need to be added to the assets folder of the Android proj
        model = modelLoader.loadModel(Gdx.files.getFileHandle("data/blob.g3db", Files.FileType.Internal))
        // Now create an instance.  Instance holds the positioning data, etc of an instance of your model
        modelInstance = ModelInstance(model!!)

        //fbx-conv is supposed to perform this rotation for you... it doesnt seem to
        modelInstance!!.transform.rotate(1f, 0f, 0f, -90f)
        // modelInstance.transform.rotate(0, 1, 0, -90);


        //move the model down a bit on the screen ( in a z-up world, down is -z ).
        modelInstance!!.transform.translate(0f, 0f, -2f)

        // Finally we want some light, or we wont see our color.  The environment gets passed in during
        // the rendering process.  Create one, then create an Ambient ( non-positioned, non-directional ) light.
        environment = Environment()
        environment!!.set(ColorAttribute(ColorAttribute.AmbientLight, 0.8f, 0.8f, 0.8f, 1.0f))

        // You use an AnimationController to um, control animations.  Each control is tied to the model instance
        controller = AnimationController(modelInstance)
        // Pick the current animation by name
        controller!!.setAnimation("Bend", 1, object : AnimationController.AnimationListener {

            override fun onEnd(animation: AnimationController.AnimationDesc) {
                // this will be called when the current animation is done.
                // queue up another animation called "balloon".
                // Passing a negative to loop count loops forever.  1f for speed is normal speed.
                controller!!.queue("Balloon", -1, 1f, null, 0f)
            }

            override fun onLoop(animation: AnimationController.AnimationDesc) {

            }

        })
    }

    override fun dispose() {
        modelBatch!!.dispose()
        model!!.dispose()
    }

    override fun render() {
        // You've seen all this before, just be sure to clear the GL_DEPTH_BUFFER_BIT when working in 3D
        Gdx.gl.glViewport(0, 0, Gdx.graphics.width, Gdx.graphics.height)
        Gdx.gl.glClearColor(1f, 1f, 1f, 1f)
        Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT or GL10.GL_DEPTH_BUFFER_BIT)

        // For some flavor, lets spin our camera around the Y axis by 1 degree each time render is called
        //camera.rotateAround(Vector3.Zero, new Vector3(0,1,0),1f);
        // When you change the camera details, you need to call update();
        // Also note, you need to call update() at least once.
        camera!!.update()

        // You need to call update on the animation controller so it will advance the animation.  Pass in frame delta
        controller!!.update(Gdx.graphics.deltaTime)
        // Like spriteBatch, just with models!  pass in the box Instance and the environment
        modelBatch!!.begin(camera)
        modelBatch!!.render(modelInstance!!, environment)
        modelBatch!!.end()
    }

    override fun resize(width: Int, height: Int) {}

    override fun pause() {}

    override fun resume() {}
}
