/*
 * Copyright (c) 2005-2010 Trident Kirill Grouchnikov. All Rights Reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *  o Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 *
 *  o Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 *  o Neither the name of Trident Kirill Grouchnikov nor the names of
 *    its contributors may be used to endorse or promote products derived
 *    from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS;
 * OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE
 * OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE,
 * EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package fr.evolya.javatoolkit.gui.animation;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;

import fr.evolya.javatoolkit.gui.animation.TimelineEngine.TridentAnimationThread;
import fr.evolya.javatoolkit.gui.animation.interpolator.PropertyInterpolator;
import fr.evolya.javatoolkit.gui.animation.interpolator.PropertyInterpolatorSource;

@SuppressWarnings("unchecked")
public class AnimationConfig {
	private static AnimationConfig config;

	private Set<UIToolkitHandler> uiToolkitHandlers;

	private Set<PropertyInterpolator> propertyInterpolators;

	private AnimationConfig.PulseSource pulseSource;

	public interface PulseSource {
		public void waitUntilNextPulse();
	}

	public static class FixedRatePulseSource implements
			AnimationConfig.PulseSource {
		private int msDelay;

		public FixedRatePulseSource(int msDelay) {
			this.msDelay = msDelay;
		}

		@Override
		public void waitUntilNextPulse() {
			try {
				Thread.sleep(this.msDelay);
			} catch (InterruptedException ie) {
				ie.printStackTrace();
			}
		}
	}

	private class DefaultPulseSource extends FixedRatePulseSource {
		DefaultPulseSource() {
			super(40);
		}
	}

	private AnimationConfig() {
		this.pulseSource = new DefaultPulseSource();

		this.uiToolkitHandlers = new HashSet<UIToolkitHandler>();
		this.propertyInterpolators = new HashSet<PropertyInterpolator>();
		ClassLoader classLoader = Thread.currentThread()
				.getContextClassLoader();
		try {
			Enumeration urls = classLoader
					.getResources("META-INF/trident-plugin.properties");
			while (urls.hasMoreElements()) {
				URL pluginUrl = (URL) urls.nextElement();
				BufferedReader reader = null;
				try {
					reader = new BufferedReader(new InputStreamReader(pluginUrl
							.openStream()));
					while (true) {
						String line = reader.readLine();
						if (line == null)
							break;
						String[] parts = line.split("=");
						if (parts.length != 2)
							continue;
						String key = parts[0];
						String value = parts[1];
						if ("UIToolkitHandler".compareTo(key) == 0) {
							try {
								Class pluginClass = classLoader
										.loadClass(value);
								if (pluginClass == null)
									continue;
								if (UIToolkitHandler.class
										.isAssignableFrom(pluginClass)) {
									UIToolkitHandler uiToolkitHandler = (UIToolkitHandler) pluginClass
											.newInstance();
									uiToolkitHandler.isHandlerFor(new Object());
									this.uiToolkitHandlers
											.add(uiToolkitHandler);
								}
							} catch (NoClassDefFoundError ncdfe) {
								// trying to initialize a plugin with a missing
								// class
							}
						}
						if ("PropertyInterpolatorSource".compareTo(key) == 0) {
							try {
								Class piSourceClass = classLoader
										.loadClass(value);
								if (piSourceClass == null)
									continue;
								if (PropertyInterpolatorSource.class
										.isAssignableFrom(piSourceClass)) {
									PropertyInterpolatorSource piSource = (PropertyInterpolatorSource) piSourceClass
											.newInstance();
									Set<PropertyInterpolator> interpolators = piSource
											.getPropertyInterpolators();
									for (PropertyInterpolator pi : interpolators) {
										try {
											Class basePropertyClass = pi
													.getBasePropertyClass();
											// is in classpath?
											basePropertyClass.getClass();
											this.propertyInterpolators.add(pi);
										} catch (NoClassDefFoundError ncdfe) {
											// trying to initialize a plugin
											// with a missing
											// class - just skip
										}

									}
									// this.propertyInterpolators.addAll(piSource
									// .getPropertyInterpolators());
								}
							} catch (NoClassDefFoundError ncdfe) {
								// trying to initialize a plugin with a missing
								// class
							}
						}
					}
				} finally {
					if (reader != null) {
						try {
							reader.close();
						} catch (IOException ioe) {
						}
					}
				}
			}
		} catch (Exception exc) {
			exc.printStackTrace();
		}
	}

	public static synchronized AnimationConfig getInstance() {
		if (config == null)
			config = new AnimationConfig();
		return config;
	}

	public synchronized Collection<UIToolkitHandler> getUIToolkitHandlers() {
		return Collections.unmodifiableSet(this.uiToolkitHandlers);
	}

	public synchronized Collection<PropertyInterpolator> getPropertyInterpolators() {
		return Collections.unmodifiableSet(this.propertyInterpolators);
	}

	public synchronized PropertyInterpolator getPropertyInterpolator(
			Object... values) {
		
		for (PropertyInterpolator interpolator : this.propertyInterpolators) {
			try {
				Class basePropertyClass = interpolator.getBasePropertyClass();
				boolean hasMatch = true;
				for (Object value : values) {
					if (!basePropertyClass.isAssignableFrom(value.getClass())) {
						hasMatch = false;
						continue;
					}
				}
				if (hasMatch)
					return interpolator;
			} catch (NoClassDefFoundError ncdfe) {
				continue;
			}
		}
		return null;
	}

	public synchronized void addPropertyInterpolator(
			PropertyInterpolator pInterpolator) {
		this.propertyInterpolators.add(pInterpolator);
	}

	public synchronized void addPropertyInterpolatorSource(
			PropertyInterpolatorSource pInterpolatorSource) {
		this.propertyInterpolators.addAll(pInterpolatorSource
				.getPropertyInterpolators());
	}

	public synchronized void removePropertyInterpolator(
			PropertyInterpolator pInterpolator) {
		this.propertyInterpolators.remove(pInterpolator);
	}

	public synchronized void addUIToolkitHandler(
			UIToolkitHandler uiToolkitHandler) {
		this.uiToolkitHandlers.add(uiToolkitHandler);
	}

	public synchronized void removeUIToolkitHandler(
			UIToolkitHandler uiToolkitHandler) {
		this.uiToolkitHandlers.remove(uiToolkitHandler);
	}

	public synchronized void setPulseSource(PulseSource pulseSource) {
		TridentAnimationThread current = TimelineEngine.getInstance().animatorThread;
		if ((current != null) && current.isAlive())
			throw new IllegalStateException(
					"Cannot replace the pulse source thread once it's running");
		this.pulseSource = pulseSource;
	}

	public synchronized AnimationConfig.PulseSource getPulseSource() {
		return pulseSource;
	}
	
}
