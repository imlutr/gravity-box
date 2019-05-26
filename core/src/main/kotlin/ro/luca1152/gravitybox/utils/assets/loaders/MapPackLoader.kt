/*
 * This file is part of Gravity Box.
 *
 * Gravity Box is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Gravity Box is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Gravity Box.  If not, see <https://www.gnu.org/licenses/>.
 */

package ro.luca1152.gravitybox.utils.assets.loaders

import com.badlogic.gdx.assets.AssetDescriptor
import com.badlogic.gdx.assets.AssetLoaderParameters
import com.badlogic.gdx.assets.AssetManager
import com.badlogic.gdx.assets.loaders.AsynchronousAssetLoader
import com.badlogic.gdx.assets.loaders.FileHandleResolver
import com.badlogic.gdx.files.FileHandle
import com.badlogic.gdx.utils.Array

class MapPackLoader(resolver: FileHandleResolver) : AsynchronousAssetLoader<MapPack, MapPackLoader.MapPackParameter>(resolver) {
    private var mapPack: MapPack? = null

    override fun loadAsync(manager: AssetManager?, fileName: String?, file: FileHandle, parameter: MapPackParameter?) {
        mapPack = null
        mapPack = MapPack(file)
    }

    override fun loadSync(manager: AssetManager?, fileName: String?, file: FileHandle?, parameter: MapPackParameter?): MapPack {
        val mapPack = this.mapPack
        this.mapPack = null

        return mapPack!!
    }

    override fun getDependencies(fileName: String?, file: FileHandle?, parameter: MapPackParameter?): Array<AssetDescriptor<Any>>? {
        return null
    }

    class MapPackParameter : AssetLoaderParameters<MapPack>()
}