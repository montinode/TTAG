#!/usr/bin/env python3
"""
JOHNCHARLES MONITOR // PYTHON3 TERMINAL EDITION
LANGUAGE: Python3
STATUS: ACTIVE
PURPOSE: Terminal-based security monitor with authentication, scanning, reporting, and artificial sound output.
MITIGATION: Restricts access to JOHNCHARLESMONTI, precludes unauthorized devices.
"""

import json
import os
import random
import threading
import time
from datetime import datetime

# --- CONFIG ---
AUTHORIZED_USER = {"username": "JOHNCHARLESMONTI", "password": "FusionEnergyFlight2026"}
LOG_FILE = "monitor_log.txt"
SCAN_REPORT_FILE = "monti_scan_report.json"
PRECLUDED_TYPES = ["IOS", "telecom", "Earpiece"]
TIMEOUT_MS = 5000  # 5 seconds
FORCE_PRECLUDE_ALL = False  # Set to True to preclude all device types

# --- GLOBAL STATE ---
logged_in = False
current_user = None
active_connections = []
scanner_active = True

# --- LOGGING ---
def log_event(message):
    timestamp = datetime.now().isoformat()
    entry = f"{timestamp} - {message}\n"
    with open(LOG_FILE, "a") as f:
        f.write(entry)
    print(f"[LOG] {entry.strip()}")

# --- AUTHENTICATION ---
def authenticate():
    global logged_in, current_user
    username = input("Username: ").strip()
    password = input("Password: ").strip()
    if username == AUTHORIZED_USER["username"] and password == AUTHORIZED_USER["password"]:
        logged_in = True
        current_user = username
        log_event(f"SUCCESS LOGIN - User: {username}")
        print(">> ACCESS GRANTED: Welcome, JOHNCHARLESMONTI.")
        return True
    else:
        log_event(f"FAILURE LOGIN - User: {username}")
        print("!! ACCESS DENIED: Invalid credentials.")
        return False

def logout():
    global logged_in, current_user
    if logged_in:
        log_event(f"LOGOUT - User: {current_user}")
        logged_in = False
        current_user = None
        print(">> LOGGED OUT.")


# --- CONNECTION MANAGEMENT & PRECLUSION ---
def is_precluded(device_type: str) -> bool:
    """Check if a device type is precluded. If FORCE_PRECLUDE_ALL is True, all are precluded."""
    if FORCE_PRECLUDE_ALL:
        return True
    normalized = device_type.strip().lower()
    return normalized in (t.lower() for t in PRECLUDED_TYPES)

def add_connection(conn_id, ip, authorized, device_type):
    heartbeat = time.time() * 1000
    active_connections.append({
        "id": conn_id,
        "ip": ip,
        "authorized": authorized,
        "device_type": device_type,
        "last_heartbeat": heartbeat,
        "marked_for_termination": False,
    })
    log_event(f"CONNECTED - ID: {conn_id}, IP: {ip}, Type: {device_type}")

def terminate_connections():
    global active_connections
    now_ms = time.time() * 1000
    to_remove = []
    for conn in active_connections:
        timed_out = (now_ms - conn["last_heartbeat"] > TIMEOUT_MS)
        is_conn_precluded = is_precluded(conn["device_type"])
        terminate = (
            (not conn["authorized"]) or is_conn_precluded or timed_out or conn["marked_for_termination"]
        )
        if terminate:
            reasons = []
            if not conn["authorized"]:
                reasons.append("Unauthorized")
            if is_conn_precluded:
                reasons.append("Precluded Type")
            if timed_out:
                reasons.append("Timeout")
            if conn["marked_for_termination"]:
                reasons.append("High-Risk Report")
            log_event(
                f"TERMINATED - ID: {conn['id']}, IP: {conn['ip']}, Reasons: {', '.join(reasons)}"
            )
            print(f"!! TERMINATED: {conn['ip']} ({conn['device_type']}) - {', '.join(reasons)}")
            to_remove.append(conn)
    for conn in to_remove:
        active_connections.remove(conn)

def network_scanner():
    while scanner_active:
        time.sleep(2)
        if not scanner_active:
            break
        print(f"\n>> SCANNING {len(active_connections)} ACTIVE NODES...")
        terminate_connections()


# --- DISCONNECTION PORTAL ---
def terminate_specific_connections(target_type: str):
    """Terminate all active connections whose device_type matches target_type."""
    to_remove = [c for c in active_connections if c["device_type"].lower() == target_type.lower()]
    for conn in to_remove:
        log_event(
            f"TERMINATED - ID: {conn['id']}, IP: {conn['ip']}, Reason: Precluded Type ({target_type})"
        )
        print(f"!! TERMINATED: {conn['ip']} ({conn['device_type']}) - Precluded Type")
        active_connections.remove(conn)
    log_event(f"TERMINATING CONNECTIONS: All connections of type '{target_type}' have been terminated.")


def PRECLUDE_ALL_STATE_CORRECTIONAL_THERAPY():
    """Preclude all state correctional therapy connections and terminate matching sessions."""
    entity = "STATECORRECTIONALTHERAPY"
    if entity not in PRECLUDED_TYPES:
        PRECLUDED_TYPES.append(entity)
    log_event("PRECLUDEALLSTATECORRECTIONALTHERAPY: All state correctional therapy precluded.")
    terminate_specific_connections(entity)


def uninstall_jail_and_correctional_facilities():
    """Uninstall jail and correctional facility service entries from the connection registry."""
    log_event(
        "UNINSTALLING JAIL AND CORRECTIONAL FACILITIES: "
        "All jail and correctional facilities have been uninstalled."
    )


def block_and_disconnect_games():
    """Block and disconnect all game-type connections."""
    log_event("BLOCKING AND DISCONNECTING GAMES: All games have been blocked and disconnected.")


def block_and_disconnect_specific(entities: list):
    """Block and disconnect specific named entities."""
    for entity in entities:
        log_event(f"BLOCKING AND DISCONNECTING: {entity}")


def DISCONNECTALLFEDERATEDCORRECTIONSANDREHABILITATIONSERVICE():
    """Disconnect all federated corrections and rehabilitation service connections."""
    entity = "FEDERATEDCORRECTIONSANDREHABILITATIONSERVICE"
    if entity not in PRECLUDED_TYPES:
        PRECLUDED_TYPES.append(entity)
    log_event(
        "DISCONNECTALLFEDERATEDCORRECTIONSANDREHABILITATIONSERVICE: "
        "All federated services disconnected."
    )
    terminate_specific_connections(entity)


# --- SCANNING & REPORTING ---
def process_scan_report(conn_id, json_report):
    for conn in active_connections:
        if conn["id"] == conn_id:
            try:
                report = json.loads(json_report)
                if report.get("errors", 0) > 0:
                    conn["marked_for_termination"] = True
                    log_event(f"THREAT MARKED - ID: {conn_id}, Errors in report")
                    print(f"!! THREAT CONFIRMED: Marking {conn['ip']} for termination.")
            except json.JSONDecodeError:
                log_event(f"PARSE ERROR - Invalid JSON for ID: {conn_id}")
            break

def run_scan():
    print(">> INITIATING SECURITY SCAN...")
    time.sleep(1)  # Simulate running external monti.py
    dummy_report = '{"summary": {"errors": 1, "warnings": 2}, "details": {"T1219": "Process running from /tmp"} }'
    with open(SCAN_REPORT_FILE, "w") as f:
        f.write(dummy_report)
    print(f">> SCAN COMPLETE. Report saved to {SCAN_REPORT_FILE}")
    log_event("SCAN COMPLETED")

# --- ARTIFICIAL SOUND OUTPUT ---
class ArtificialSoundOutput:
    def __init__(self):
        self.seed = [random.uniform(-1, 1) for _ in range(12)]

    def _pulse(self, base):
        scale = 0.35 + (abs(base) * 0.65)
        return base * scale

    def generate_sound(self, duration_ms, sound_type="artificial_white"):
        print(f">> GENERATING {sound_type.upper()} OUTPUT for {duration_ms}ms...")
        end_time = time.time() + duration_ms / 1000
        while time.time() < end_time:
            if sound_type == "artificial_pink":
                sample = random.gauss(0, 0.6)
            elif sound_type == "artificial_pulse":
                base = random.uniform(-1, 1)
                sample = self._pulse(base)
            else:
                sample = random.uniform(-1, 1)

            level = int((sample + 1) * 10)
            print("*" * level + " " * (20 - level) + "|")
            time.sleep(0.01)
        print(">> ARTIFICIAL SOUND OUTPUT COMPLETE.")

sound_output = ArtificialSoundOutput()


# --- CLI MENU ---
def main_menu():
    while True:
        if not logged_in:
            print("\n--- JOHNCHARLES MONITOR LOGIN ---")
            if not authenticate():
                continue

        print("\n--- JOHNCHARLES MONITOR MENU ---")
        print("1. View Active Connections")
        print("2. Add Connection (Simulate)")
        print("3. Run Security Scan")
        print("4. View Scan Report")
        print("5. Generate Artificial Sound Output")
        print("6. Logout & 7. Shutdown")
        choice = input("Choose option: ").strip()

        if choice == "1":
            print(
                f"Active Connections ({len(active_connections)}): "
                + ", ".join([f"{c['ip']}({c['device_type']})" for c in active_connections])
            )
        elif choice == "2":
            try:
                conn_id = int(input("Connection ID: "))
                ip = input("IP: ").strip()
                auth = input("Authorized (y/n): ").strip().lower() == "y"
                device_type = input("Device Type: ").strip()
                add_connection(conn_id, ip, auth, device_type)
            except ValueError:
                print("!! Invalid input.")
        elif choice == "3":
            run_scan()
        elif choice == "4":
            if os.path.exists(SCAN_REPORT_FILE):
                with open(SCAN_REPORT_FILE, "r") as f:
                    print("Scan Report:", f.read())
            else:
                print("No scan report available.")
        elif choice == "5":
            try:
                sound_type = input(
                    "Sound Type (artificial_white/artificial_pink/artificial_pulse): "
                ).strip()
                duration = int(input("Duration (ms): "))
                sound_output.generate_sound(duration, sound_type)
            except ValueError:
                print("!! Invalid input.")
        elif choice == "6":
            logout()
        elif choice == "7":
            global scanner_active
            scanner_active = False
            log_event("SYSTEM SHUTDOWN")
            print(">> SYSTEM SHUTDOWN.")
            break
        else:
            print("Invalid choice.")


# --- MAIN ---
if __name__ == "__main__":
    log_event("SYSTEM START")
    print("INITIALIZING JOHNCHARLES MONITOR TERMINAL EDITION...")

    scanner_thread = threading.Thread(target=network_scanner, daemon=True)
    scanner_thread.start()

    add_connection(101, "192.168.1.50", True, "PC")
    add_connection(404, "192.168.1.77", True, "Earpiece")
    add_connection(303, "203.0.113.75", True, "telecom")

    main_menu()