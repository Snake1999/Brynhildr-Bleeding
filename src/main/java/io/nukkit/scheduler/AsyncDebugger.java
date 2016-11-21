package io.nukkit.scheduler;

class AsyncDebugger {
    private AsyncDebugger next = null;
    private final int expiry;
    private final TaskOwner owner;
    private final Class clazz;

    AsyncDebugger(int expiry, TaskOwner owner, Class clazz) {
        this.expiry = expiry;
        this.owner = owner;
        this.clazz = clazz;
    }

    final AsyncDebugger getNextHead(int time) {
        AsyncDebugger current;
        AsyncDebugger next;
        for (current = this; time > current.expiry; current = next) {
            next = current.next;
            if (current.next == null) {
                break;
            }
        }

        return current;
    }

    final AsyncDebugger setNext(AsyncDebugger next) {
        return this.next = next;
    }

    StringBuilder debugTo(StringBuilder string) {
        for (AsyncDebugger next = this; next != null; next = next.next) {
            string.append(next.owner.toString()).append(':').append(next.clazz.getName()).append('@').append(next.expiry).append(',');
        }

        return string;
    }
}
