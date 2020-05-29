/*
 * This file is part of Technic Minecraft Core.
 * Copyright ©2015 Syndicate, LLC
 *
 * Technic Minecraft Core is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Technic Minecraft Core is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License,
 * as well as a copy of the GNU Lesser General Public License,
 * along with Technic Minecraft Core.  If not, see <http://www.gnu.org/licenses/>.
 */

package net.technicpack.minecraftcore.install.tasks;

import net.technicpack.launchercore.install.InstallTasksQueue;
import net.technicpack.launchercore.install.tasks.ListenerTask;
import net.technicpack.launchercore.install.verifiers.IFileVerifier;
import net.technicpack.launchercore.install.verifiers.MD5FileVerifier;
import net.technicpack.launchercore.install.verifiers.ValidZipFileVerifier;
import net.technicpack.launchercore.modpacks.ModpackModel;
import net.technicpack.minecraftcore.MojangUtils;
import net.technicpack.minecraftcore.mojang.version.MojangVersion;
import net.technicpack.minecraftcore.mojang.version.io.GameDownloads;

import java.io.File;
import java.io.IOException;

public class InstallMinecraftIfNecessaryTask extends ListenerTask {

	private final ModpackModel pack;
	private final String minecraftVersion;
	private final File cacheDirectory;

	public InstallMinecraftIfNecessaryTask(ModpackModel pack, String minecraftVersion, File cacheDirectory) {
		this.pack = pack;
		this.minecraftVersion = minecraftVersion;
		this.cacheDirectory = cacheDirectory;
	}

	@Override
	public String getTaskDescription() {
		return "Installing Minecraft";
	}

	@Override
	public void runTask(InstallTasksQueue queue) throws IOException, InterruptedException {
		super.runTask(queue);

		MojangVersion version = ((InstallTasksQueue<MojangVersion>)queue).getMetadata();

		String url;
		GameDownloads dls = version.getDownloads();
		if (dls != null) {
			url = dls.forClient().getUrl(); // TODO maybe use the sha1 sum?
		} else {
			url = MojangUtils.getVersionDownload(this.minecraftVersion);
		}
		String md5 = queue.getMirrorStore().getETag(url);
		File cache = new File(cacheDirectory, "minecraft_" + this.minecraftVersion + ".jar");

		IFileVerifier verifier;

		if (md5 != null && !md5.isEmpty()) {
			verifier = new MD5FileVerifier(md5);
		} else {
			verifier = new ValidZipFileVerifier();
		}

		if (!cache.exists() || !verifier.isFileValid(cache)) {
			String output = this.pack.getCacheDir() + File.separator + "minecraft.jar";
			queue.getMirrorStore().downloadFile(url, cache.getName(), output, cache, verifier, this);
		}

		MojangUtils.copyMinecraftJar(cache, new File(this.pack.getBinDir(), "minecraft.jar"));
	}

}
