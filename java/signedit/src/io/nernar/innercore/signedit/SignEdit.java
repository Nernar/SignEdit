package io.nernar.innercore.signedit;

import com.zhekasmirnov.innercore.api.NativeAPI;
import com.zhekasmirnov.innercore.api.NativeTileEntity;
import com.zhekasmirnov.innercore.api.commontypes.Coords;
import com.zhekasmirnov.innercore.api.commontypes.ItemInstance;
import com.zhekasmirnov.innercore.api.log.ICLog;
import com.zhekasmirnov.innercore.api.mod.ScriptableObjectHelper;
import com.zhekasmirnov.innercore.api.mod.util.ScriptableFunctionImpl;
import com.zhekasmirnov.innercore.api.runtime.Callback;
import com.zhekasmirnov.innercore.api.unlimited.IDRegistry;
import com.zhekasmirnov.innercore.mod.build.Config;
import java.util.ArrayList;
import java.util.List;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;
import vsdum.kex.util.AddonUtils;
import vsdum.kex.natives.GlobalContext;
import vsdum.kex.natives.LocalPlayer;

/**
 * Copyright 2022-2024 Nernar (github.com/nernar)
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
public class SignEdit {
	public static final int SIGN_TILE_ENTITY_TYPE = 4;
	public static final String[] VANILLA_SIGN_IDS = new String[] { "oak_sign", "birch_sign", "spruce_sign",
			"dark_oak_sign", "acacia_sign", "jungle_sign", "warped_sign", "crimson_sign" };

	private static final List<Integer> registeredIds = new ArrayList<>();
	private static boolean signRequiredToEdit = true;

	public static boolean isSignRequiredToEdit() {
		return signRequiredToEdit;
	}

	public static void setIsSignRequiredToEdit(boolean required) {
		signRequiredToEdit = required;
	}

	private static final class ItemUseLocal extends ScriptableFunctionImpl {
		public Object call(Context context, Scriptable scriptable, Scriptable scope, Object[] objects) {
			long playerUid = ((Long) objects[3]).longValue();
			if (NativeAPI.isSneaking(playerUid)) {
				return null;
			}

			if (signRequiredToEdit) {
				ItemInstance item = (ItemInstance) objects[1];
				Integer id = Integer.valueOf(item.getId());
				if (registeredIds.indexOf(id) == -1) {
					return null;
				}
			}

			Coords coords = (Coords) objects[0];
			int x = ((Integer) coords.get("x")).intValue();
			int y = ((Integer) coords.get("y")).intValue();
			int z = ((Integer) coords.get("z")).intValue();

			NativeTileEntity tile = NativeTileEntity.getTileEntity(x, y, z);
			if (tile != null && tile.getType() == SIGN_TILE_ENTITY_TYPE) {
				NativeAPI.preventDefault();
				LocalPlayer player = GlobalContext.getLocalPlayer();
				if (player.getPlayerPermissionLevel() > 0) {
					player.openSign(x, y, z);
				}
			}
			return null;
		}
	}

	private static final class BlocksDefined extends ScriptableFunctionImpl {
		public Object call(Context context, Scriptable scriptable, Scriptable scope, Object[] objects) {
			ScriptableObject object = ScriptableObjectHelper.createEmpty();
			object.put("isSignRequiredToEdit", object, new ScriptableFunctionImpl() {
				public Object call(Context context, Scriptable scriptable, Scriptable scope, Object[] objects) {
					return Boolean.valueOf(isSignRequiredToEdit());
				}
			});
			object.put("setIsSignRequiredToEdit", object, new ScriptableFunctionImpl() {
				public Object call(Context context, Scriptable scriptable, Scriptable scope, Object[] objects) {
					setIsSignRequiredToEdit(((Boolean) objects[0]).booleanValue());
					return null;
				}
			});
			object.put("registerSign", object, new ScriptableFunctionImpl() {
				public Object call(Context context, Scriptable scriptable, Scriptable scope, Object[] objects) {
					if (objects[0] instanceof Number) {
						registerSign(((Integer) objects[0]).intValue());
						return null;
					}
					registerSign((String) objects[0]);
					return null;
				}
			});
			object.put("isRegisteredSign", object, new ScriptableFunctionImpl() {
				public Object call(Context context, Scriptable scriptable, Scriptable scope, Object[] objects) {
					if (objects[0] instanceof Number) {
						return Boolean.valueOf(isRegisteredSign(((Integer) objects[0]).intValue()));
					}
					return Boolean.valueOf(isRegisteredSign((String) objects[0]));
				}
			});
			Callback.invokeAPICallback("API:SignEdit", new Object[] { object });
			return null;
		}
	}

	static {
		for (String vanillaId : VANILLA_SIGN_IDS) {
			registeredIds.add(AddonUtils.getNumericIdFromIdentifier(vanillaId));
		}
		Callback.addCallback("ItemUseLocal", new ItemUseLocal(), 0);
		Callback.addCallback("BlocksDefined", new BlocksDefined(), 0);
	}

	public static boolean isRegisteredSign(String namedId) {
		return isRegisteredSign(IDRegistry.getIDByName(namedId));
	}

	public static boolean isRegisteredSign(int id) {
		Integer indexId = Integer.valueOf(id);
		return registeredIds.indexOf(indexId) != -1;
	}

	public static void registerSign(int id) {
		Integer indexId = Integer.valueOf(id);
		synchronized (registeredIds) {
			if (registeredIds.indexOf(indexId) == -1) {
				registeredIds.add(indexId);
				return;
			}
		}
		ICLog.d(SignEdit.class.getSimpleName(), "Sign id " + id + " already registered");
	}

	public static void registerSign(String namedId) {
		int uid = IDRegistry.getIDByName(namedId);
		if (uid == 0) {
			ICLog.e(SignEdit.class.getSimpleName(), "Sign id " + namedId + " not even registered!",
					new NullPointerException(namedId + " == 0"));
			return;
		}
		for (String id : VANILLA_SIGN_IDS) {
			if (id.equals(namedId)) {
				ICLog.i(SignEdit.class.getSimpleName(), "Sign id " + namedId + " consist with vanilla");
				return;
			}
		}
		registerSign(uid);
	}

	public static void setConfig(Config config) {
		signRequiredToEdit = config.getBool("sign_required_to_edit");
	}
}
