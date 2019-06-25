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

@file:JvmName("DesktopLauncher")
@file:Suppress("UnusedMainParameter")

package ro.luca1152.gravitybox.desktop

import com.amazonaws.ClientConfiguration
import com.amazonaws.auth.PropertiesCredentials
import com.amazonaws.internal.StaticCredentialsProvider
import com.amazonaws.regions.Region
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBAsyncClient
import com.badlogic.gdx.Files
import com.badlogic.gdx.backends.lwjgl.LwjglApplication
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration
import ro.luca1152.gravitybox.MyGame
import java.io.File

/** Launches the desktop (LWJGL) application. */
fun main(args: Array<String>) {
    LwjglApplication(MyGame().apply {
        dynamoDBClient = createDynamoDBClient()
    }, LwjglApplicationConfiguration().apply {
        title = "Gravity Box"
        width = 540
        height = 960
        resizable = false
        samples = 4
        intArrayOf(128, 64, 32, 16).forEach {
            addIcon("gravity-box-$it.png", Files.FileType.Internal)
        }
    })
}

private fun createDynamoDBClient() = AmazonDynamoDBAsyncClient(
    StaticCredentialsProvider(PropertiesCredentials(File("C:\\Users\\luktr\\.aws\\credentials"))),
    ClientConfiguration()
        .withConnectionTimeout(500)
        .withSocketTimeout(1000)
        .withMaxErrorRetry(Integer.MAX_VALUE)
).apply {
    endpoint = "dynamodb.eu-central-1.amazonaws.com"
    setRegion(Region.getRegion("eu-central-1"))
}

