package mindustry.entities.bullet;

import arc.math.geom.*;
import arc.util.*;
import mindustry.*;
import mindustry.entities.*;
import mindustry.gen.*;

public class PointBulletType extends BulletType{
     private static final ThreadLocal<Float> cdist = ThreadLocal.withInitial(() -> 0f);
     private static final ThreadLocal<Unit> result = new ThreadLocal<>();

     public float trailSpacing = 10f;

     public PointBulletType(){
         scaleLife = true;
         lifetime = 100f;
         collides = false;
         reflectable = false;
         keepVelocity = false;
     }

    @Override
    public void init(Bullet b){
        super.init(b);

        float px = b.x + b.lifetime * b.vel.x,
            py = b.y + b.lifetime * b.vel.y,
            rot = b.rotation();

        Geometry.iterateLine(0f, b.x, b.y, px, py, trailSpacing, (x, y) -> {
            trailEffect.at(x, y, rot);
        });

        b.time = b.lifetime;
        b.set(px, py);

        //calculate hit entity

        cdist.set(0f);
        result.set(null);
        float range = 1f;

        Units.nearbyEnemies(b.team, px - range, py - range, range*2f, range*2f, e -> {
            if(e.dead() || !e.checkTarget(collidesAir, collidesGround) || !e.hittable()) return;

            e.hitbox(Tmp.r1);
            if(!Tmp.r1.contains(px, py)) return;

            float dst = e.dst(px, py) - e.hitSize;
            if((result.get() == null || dst < cdist.get())){
                result.set(e);
                cdist.set(dst);
            }
        });

        if(result.get() != null){
            b.collision(result.get(), px, py);
        }else if(collidesTiles){
            Building build = Vars.world.buildWorld(px, py);
            if(build != null && build.team != b.team){
                build.collision(b);
                hit(b, px, py);
                b.hit = true;
            }
        }

        b.remove();

        b.vel.setZero();
    }
}
