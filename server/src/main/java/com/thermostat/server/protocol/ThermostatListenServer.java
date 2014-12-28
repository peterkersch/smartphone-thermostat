package com.thermostat.server.protocol;

/**
 * Listens for incoming connections over a specific technology.
 * Currently implemented technologies are
 * - Bluetooth over RFCOMM socket
 * - Generic IP (IPv4 or IPv6 link local)
 */
public abstract class ThermostatListenServer extends Thread {

}
