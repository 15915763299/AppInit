package com.demo.appinit.anchors;

class LockableTask extends BaseTask {

    private LockableAnchor lockableAnchor;

    LockableTask(BaseTask wait, LockableAnchor lockableAnchor) {
        super(wait.getId() + "_waiter",true);
        lockableAnchor.setTargetTaskId(wait.getId());
        this.lockableAnchor = lockableAnchor;
    }

    @Override
    protected void run(String name) {
        lockableAnchor.lock();
    }

    boolean successToUnlock(){
        return lockableAnchor.successToUnlock();
    }
}
