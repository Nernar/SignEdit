package io.nernar.innercore.signedit

import com.zhekasmirnov.innercore.api.*
import com.zhekasmirnov.innercore.api.commontypes.*
import com.zhekasmirnov.innercore.api.log.*
import com.zhekasmirnov.innercore.api.mod.*
import com.zhekasmirnov.innercore.api.mod.util.*
import com.zhekasmirnov.innercore.api.runtime.*
import com.zhekasmirnov.innercore.api.unlimited.*
import com.zhekasmirnov.innercore.mod.build.*
import java.lang.reflect.*
import java.util.*
import org.mozilla.javascript.*
import vsdum.kex.natives.*

class SignEdit {
	public final val SIGN_TILE_ENTITY_TYPE = 4
	public final val VANILLA_SIGN_IDS = listOf("oak_sign", "birch_sign", "spruce_sign", "dark_oak_sign",
											   "acacia_sign", "jungle_sign", "warped_sign", "crimson_sign");
	
	private val registeredIds = ArrayList<Integer>()
	private var signRequiredToEdit = true
	
	public fun isSignRequiredToEdit(): Boolean = signRequiredToEdit
	
	public fun setIsSignRequiredToEdit(required: Boolean) {
		signRequiredToEdit = required
	}
	
	final object ItemUseServer : ScriptableFunctionImpl() {
		override fun call(context: Context, scriptable: Scriptable, scope: Scriptable, objects: Array<Any>): Object? {
			val playerUid: Long = objects[3] as Long
			if (NativeAPI.isSneaking(playerUid)) {
				return null
			}
			
			if (SignEdit::signRequiredToEdit) {
				val item: ItemInstance = objects[1] as ItemInstance
				if (SignEdit::registeredIds.indexOf(item.getId() as Integer) == -1) {
					return null
				}
			}
			
			val coords: Coords = objects[0] as Coords
			val x: Int = coords.get("x") as Int
			val y: Int = coords.get("y") as Int
			val z: Int = coords.get("z") as Int
			
			val tile = NativeTileEntity.getTileEntity(x, y, z)
			if (tile != null && tile.getType() == SignEdit::SIGN_TILE_ENTITY_TYPE) {
				NativeAPI.preventDefault()
				try {
					val player = GlobalContext.getLocalPlayer()
					if (player.getPlayerPermissionLevel() > 0) {
						player.openSign(x, y, z)
					}
				} catch (e: Exception) {
					ICLog.e(SignEdit::class.qualifiedName, "Something went wrong when sign text replacement dialog creation", e)
				}
			}
			return null
		}
	}
	
	final object BlocksDefined : ScriptableFunctionImpl() {
		override fun call(context: Context, scriptable: Scriptable, scope: Scriptable, objects: Array<Any>): Object? {
			val script = ScriptableObjectHelper.createEmpty()
			script.put("isSignRequiredToEdit", script, object : ScriptableFunctionImpl() {
				override fun call(context: Context, scriptable: Scriptable, scope: Scriptable, objects: Array<Any>): Object? {
					return SignEdit::isSignRequiredToEdit()
				}
			});
			script.put("setIsSignRequiredToEdit", script, object : ScriptableFunctionImpl() {
				override fun call(context: Context, scriptable: Scriptable, scope: Scriptable, objects: Array<Any>): Object? {
					SignEdit::setIsSignRequiredToEdit(objects[0] as Boolean)
					return null
				}
			});
			script.put("registerSign", script, object : ScriptableFunctionImpl() {
				override fun call(context: Context, scriptable: Scriptable, scope: Scriptable, objects: Array<Any>): Object? {
					if (objects[0] is Number) {
						SignEdit::registerSign(objects[0] as Int)
						return null
					}
					SignEdit::registerSign(objects[0] as String)
					return null
				}
			});
			script.put("isRegisteredSign", script, object : ScriptableFunctionImpl() {
				override fun call(context: Context, scriptable: Scriptable, scope: Scriptable, objects: Array<Any>): Object? {
					if (objects[0] is Number) {
						return SignEdit::isRegisteredSign(objects[0] as Int)
					}
					return SignEdit::isRegisteredSign(objects[0] as String)
				}
			});
			Callback.invokeAPICallback("API:SignEdit", listOf(script))
			return null
		}
	}
	
	init {
		Callback.addCallback("API:KernelExtension", object : ScriptableFunctionImpl() {
			override fun call(context: Context, scriptable: Scriptable, scope: Scriptable, objects: Array<Any>): Object? {
				Callback.addCallback("ItemUseServer", ItemUseServer, 0)
				return null
			}
		}, 0);
		Callback.addCallback("BlocksDefined", BlocksDefined, 0)
		ICLog.d("Kotlin", "Welcomed you")
	}
	
	public fun isRegisteredSign(namedId: String): Boolean =
		isRegisteredSign(IDRegistry.getIDByName(namedId))
	
	public fun isRegisteredSign(indexId: Int): Boolean =
		registeredIds.indexOf(indexId as Integer) != -1
	
	public fun registerSign(indexId: Int) {
		synchronized (registeredIds) {
			val integerIndexId: Integer = indexId as Integer
			if (registeredIds.indexOf(integerIndexId) == -1) {
				registeredIds.add(integerIndexId)
				return
			}
		}
		ICLog.d(SignEdit::class.qualifiedName, "Sign id $indexId already registered")
	}
	
	public fun registerSign(namedId: String) {
		val uid = IDRegistry.getIDByName(namedId)
		if (uid == 0) {
			ICLog.e(SignEdit::class.qualifiedName, "Sign id $namedId not even registered!", NullPointerException(namedId + " == 0"))
			return
		}
		for (id in VANILLA_SIGN_IDS) {
			if (id.equals(namedId)) {
				ICLog.i(SignEdit::class.qualifiedName, "Sign id $namedId consist with vanilla")
				return
			}
		}
		registerSign(uid)
	}
	
	public fun setConfig(config: Config) {
		signRequiredToEdit = config.getBool("sign_required_to_edit")
	}
	
	public fun boot(sources: HashMap<String, String>) {
		try {
			val field = IDRegistry::class.java.getDeclaredField("vanillaIdShortcut")
			field.setAccessible(true)
			val vanillaIdShortcut: HashMap<String, Integer>? = field.get(null) as? HashMap<String, Integer>
			VANILLA_SIGN_IDS.forEach {
				val shortcut = vanillaIdShortcut?.get(it)
				registeredIds.add(shortcut as Integer)
			}
		} catch (aww: Exception) {
			throw IllegalStateException("SignEdit fail on vanilla ID registration, ensure that you are uses last version of Inner Core", aww)
		}
	}
}
