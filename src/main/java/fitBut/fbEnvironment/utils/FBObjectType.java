package fitBut.fbEnvironment.utils;

/**
 * @author : Vaclav Uhlir
 * @since : 12.9.2019
 **/
public enum FBObjectType {
    __Unknown(-1),
    __FBClear(0),
    __FBObstacle(1),
    __FBThing(2),
    __FBEntity_Friend(3),
    __FBEntity_Enemy(4),
    __FBMarker(5),
    __FBBorder(6),
    __FBGoal(7),
    __FBAgent(8),
    __FBBlock(50),
    __FBTaskBoard(70),
    __FBDispenser(100);
    int value;

    FBObjectType(int i) {
        value = i;
    }
}