package com.satir.antievakuator.realm;

import com.satir.antievakuator.realm.models.EventStatus;
import com.satir.antievakuator.realm.models.ReceivedEvent;
import com.satir.antievakuator.realm.models.UserCar;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import io.realm.Realm;
import io.realm.RealmResults;

import static com.satir.antievakuator.data.Constants.FieldNameConstants.CAR_NUMBER;
import static com.satir.antievakuator.data.Constants.FieldNameConstants.DATE;
import static com.satir.antievakuator.data.Constants.FieldNameConstants.MESSAGE;
import static com.satir.antievakuator.data.Constants.FieldNameConstants.PHOTO_URL;
import static com.satir.antievakuator.data.Constants.FieldNameConstants.URL_STATIC_MAP;
import static com.satir.antievakuator.data.Constants.FieldNameConstants.USER_PHONE;

public class Database {
    private Realm mRealm;

    public Database(){
        mRealm = Realm.getDefaultInstance();
    }

    public int createNewReceivedEvent(final Map<String, String> dataMap) {
        final int[] nextID = new int[1];
        mRealm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                Number maxId = realm.where(ReceivedEvent.class).max("id");
                if (maxId == null) {
                    nextID[0] = 1;
                } else {
                    nextID[0] = maxId.intValue() + 1;
                }
                ReceivedEvent newEvent = realm.createObject(ReceivedEvent.class, nextID[0]);
                newEvent.setCarNumber(dataMap.get(CAR_NUMBER));
                newEvent.setMessage(dataMap.get(MESSAGE));
                newEvent.setPhoneNumber(dataMap.get(USER_PHONE));
                newEvent.setUrlPhoto(dataMap.get(PHOTO_URL));
                newEvent.setUrlStaticMap(dataMap.get(URL_STATIC_MAP));
                newEvent.setDate(Long.parseLong(dataMap.get(DATE)));
                newEvent.setDonateStatus(EventStatus.NOT_ANSWERED);
            }
        });
        return nextID[0];
    }

    public List<ReceivedEvent> getReceivedEvents(){
        List<ReceivedEvent> events = mRealm.where(ReceivedEvent.class).findAll();
        return events;
    }

    public ReceivedEvent getReceivedEvent(int id){
        return  mRealm.where(ReceivedEvent.class).equalTo("id", id).findFirst();
    }

    public List<UserCar> getUserCars(){
        RealmResults<UserCar> results = mRealm.where(UserCar.class).findAll();
        List<UserCar> cars = new ArrayList<>(results);
        return cars;
    }

    public void createNewUserCar(final String carNumber, final String brand, final String model){
        mRealm.executeTransactionAsync(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                realm.copyToRealm(new UserCar(carNumber, brand, model));
            }
        });
    }

    public void updateCarsInfo(final List<UserCar> cars){
        mRealm.executeTransactionAsync(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                realm.where(UserCar.class).findAll().deleteAllFromRealm();
                for(UserCar car : cars){
                    realm.copyToRealm(new UserCar(car.getCarNumber(), car.getBrand(), car.getModel()));
                }
            }
        });
    }

    public void deleteAllCars(){
        mRealm.executeTransactionAsync(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                realm.where(UserCar.class).findAll().deleteAllFromRealm();
            }
        });
    }

    public void closeRealm(){
        if(mRealm != null && !mRealm.isClosed()) {
            mRealm.close();
            mRealm = null;
        }
    }
}
