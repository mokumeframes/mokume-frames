"""Authored campaign objectives. All sequencing/scenarios are mod adaptations.

Official terminology and rescue-exam context: https://heroaca.com/special/terminology.html
and https://heroaca.com/episodes/3rd/ep57/ (checked 2026-10-28).
The reserved encounter sets are compressed exercises, not finished named world regions.
"""
from pathlib import Path
import json
ROOT=Path(__file__).resolve().parents[1]

def s(id,kind,role,text,pos=(24,1,12),seconds=0,count=1):
    return dict(id=id,type=kind,role=role,count=count,seconds=seconds,x=pos[0],y=pos[1],z=pos[2],text=text)
def move(id,text,pos):return s(id,'MOVE',id,text,pos)
def aid(id,text,pos=(14,2,30)):return s(id,'AID',id,text,pos,5)
def interact(id,text,pos=(35,1,8)):return s(id,'INTERACT',id,text,pos)
def fight(id,text,boss=False):return s(id,'BOSS' if boss else 'COMBAT',id,text,(35,1,35),count=1 if boss else 2)
def escort(id,text):return s(id,'ESCORT',id,text,(39,1,40))
def defend(id,text):return s(id,'DEFEND',id,text,(36,1,26),15)
def judgment(id,text):return s(id,'JUDGMENT','rescue_first',text,(24,1,8))

def main():
    rows=[]
    def add(id,name,path,stages,credits=100,mastery=3,licence=0,required=0,season=0):
        previous=next((m['id'] for m in reversed(rows) if m['path']==path),'')
        rows.append(dict(id=id,name=name,path=path,prerequisite=previous,licenceRequired=required,credits=credits,mastery=mastery,grantsLicence=licence,season=season,source='Mod-original scenario and sequencing; school/USJ/licence terminology informed by the official anime terminology page. Reserved compressed exercise set, not exact canon geography.',stages=stages))
    add('school_assessment','Application / practical assessment','hero',[
        interact('briefing','Read the assessment briefing at the marked console.'),fight('assessment_robots','Disable two assessment sentinels, or hold the shutdown console for eight uninterrupted seconds.'),aid('assessment_rescue','Reach the flood platform and provide first aid for five seconds.'),move('assessment_exit','Return to the entrance checkpoint.',(8,1,8))])
    add('school_orientation','Orientation / a place at U.A.','hero',[
        interact('student_record','Collect your student record from the marked desk.',(12,1,12)),move('workshop','Inspect the support-workshop station.',(40,1,10)),interact('dorm_briefing','Read the dorm and survival-equipment briefing.',(9,1,39)),interact('training_plan','Confirm your supervised training plan.',(24,1,8))])
    add('school_fundamentals','Fundamentals / control and judgement','hero',[
        move('cover_a','Navigate to the first cover marker.',(20,1,18)),move('cover_b','Keep clear of the active hazard lane and reach the second marker.',(28,1,22)),judgment('civilian_priority','A civilian is trapped while an opponent withdraws. Choose the safe response at the decision console.'),aid('first_aid','Carry out the rescue you prioritised.',(14,2,30)),defend('safe_zone','Hold the marked shelter for fifteen seconds. Step away from incoming blast tells and return; progress pauses outside the shelter.')])
    add('school_battle_trial','Battle trial / teamwork before score','hero',[
        escort('classmate','Lead your classmate from the entrance to the extraction pad. Stay within eight blocks; cover and collision still matter.'),fight('opposition','Clear the opposing exercise sentinels or reach their shutdown console.'),interact('objective','Secure the objective console.',(35,1,8))])
    add('school_usj','USJ / four rescue stations','hero',[
        move('flood_entry','Enter the marked flood-training lane.',(10,1,26)),aid('flood_rescue','Treat the stranded trainee on the platform.',(14,2,30)),interact('fire_valve','Reach the fire-control valve using the clear corridor. The red floor lane is hazardous until the valve is closed.',(40,1,10)),move('rubble_route','Use either gap around the collapsed-wall simulation.',(28,1,27)),escort('evacuation','Guide the evacuee to the extraction pad without leaving them behind.')],season=1)
    add('school_festival','Sports Festival / participation assessment','hero',[
        move('race_one','Reach the first course marker; winning a race is not required.',(18,1,8)),move('race_two','Navigate the rubble-wall opening.',(28,1,27)),move('race_three','Reach the final movement checkpoint.',(42,1,42)),aid('team_support','Support an exhausted teammate.',(12,1,36)),fight('exhibition','Complete the exhibition against exercise sentinels or use tactical shutdown.')],season=2)
    add('school_internship','Supervised internship / city response','hero',[
        interact('dispatch','Read the supervised agency dispatch.'),move('patrol_one','Check the first reported location.',(10,1,12)),move('patrol_two','Check the service passage.',(42,1,24)),judgment('incident_priority','A bystander needs help beside an unattended threat. Choose your response.'),aid('bystander','Stabilise the bystander.',(12,1,36)),fight('containment','Contain the exercise offenders or disable their relay.')],season=2)
    add('school_provisional_exam','Provisional licence / practical examination','hero',[
        judgment('exam_judgment','The exam grades rescue procedure and judgement. Choose an approach at the decision console.'),aid('exam_rescue','Provide first aid before pursuing the threat.',(14,2,30)),escort('exam_teamwork','Work with the examiner playing an evacuee; lead them to safety.'),defend('exam_protection','Maintain a protected shelter for fifteen seconds.'),fight('exam_combat','Resolve the combat station through restraint or shutdown.'),interact('exam_report','Submit the completed practical record.',(24,1,8))],credits=250,mastery=5,licence=1,season=3)
    add('school_work_study','Licensed work study / field protection','hero',[
        interact('licensed_dispatch','Accept the licensed field assignment.'),defend('field_shelter','Protect the field shelter through the timed pressure drill.'),aid('field_medic','Assist the injured field partner.',(12,1,36)),fight('field_sentinel','Defeat Bulwark, the mod-original training sentinel, or hold its emergency shutdown console.',True),interact('field_report','Report the field outcome.',(24,1,8))],credits=300,mastery=5,required=1,season=4)
    add('school_graduation','Advanced qualification / final operation','hero',[
        judgment('leadership','Choose the priority for the final operation.'),escort('final_evacuation','Lead the evacuation across the exercise set.'),aid('final_support','Recover the exhausted partner.',(12,1,36)),fight('final_bulwark','Complete the phased Bulwark encounter using positioning, cover and its punishable recovery.',True),interact('qualification_record','Submit the full school record. Professional status here is the mod\'s qualification adaptation.',(24,1,8))],credits=500,mastery=5,licence=2,required=1)
    villain=[
        ('underground_introduction','Underground / first contact',[interact('contact','Read the underground contact brief.'),move('quiet_route','Scout the service route without entering the hazard lane.',(42,1,24)),interact('signal','Recover the contact signal.',(9,1,39))]),
        ('contact_contract','Contact work / contested information',[move('observation','Reach the observation point.',(20,1,18)),interact('records','Retrieve the exercise intelligence.',(35,1,8)),escort('informant','Extract the informant safely.')]),
        ('resource_operation','Resources / guarded relay',[fight('guards','Bypass the guards with relay shutdown, or defeat the exercise sentinels.'),interact('supplies','Secure the exercise supply record.',(9,1,39)),move('extraction','Reach the extraction point.',(8,1,8))]),
        ('faction_recruitment','Recruitment / prove reliability',[aid('crew_aid','Treat an injured prospective ally.',(12,1,36)),defend('crew_shelter','Hold the crew shelter through the pressure exercise.'),interact('recruitment','Confirm the recruitment terms.',(24,1,8))]),
        ('agency_infiltration','Infiltration / agency simulation',[move('entry_route','Reach the service entrance.',(42,1,24)),move('inner_route','Navigate around the collapsed wall.',(28,1,27)),interact('archive','Recover the marked agency-simulation record.',(12,1,12)),move('clean_exit','Return through the clear extraction route.',(8,1,8))]),
        ('guarded_shipment','Interception / guarded shipment',[fight('escort_guards','Disable the shipment guards or their control relay.'),interact('manifest','Recover the contested manifest.',(35,1,8)),escort('courier','Extract the courier.')]),
        ('rival_encounter','Rivals / contested ground',[defend('position','Hold the marked position under pressure.'),fight('rival_sentinel','Defeat or disable the rival exercise sentinel.',True)]),
        ('hideout_development','Hideout / protect the workshop',[interact('workshop_plan','Inspect the safehouse workshop plan.',(40,1,10)),aid('engineer','Assist the workshop engineer.',(12,1,36)),defend('workshop_defence','Protect the workshop shelter.'),interact('safehouse_record','Register the earned safehouse access.',(9,1,39))]),
        ('rising_notoriety','Notoriety / extraction under pursuit',[escort('crew_extraction','Lead the pursued crew member to safety.'),fight('pursuit','Contain or disable the pursuing exercise sentinels.'),interact('operation_record','Report the operation.',(24,1,8))]),
        ('crew_leadership','Leadership / rival operation',[judgment('crew_priority','A crew member is injured during the operation. Choose your priority.'),aid('crew_recovery','Carry out the recovery.',(12,1,36)),fight('leadership_boss','Resolve the phased rival-sentinel encounter.',True),interact('leadership_record','Submit the leadership record.',(24,1,8))])]
    for i,(id,name,stages) in enumerate(villain):add(id,name,'villain',stages,150+i*20,3)
    independent=[
        ('neighbourhood_watch','Independent / neighbourhood watch',[move('watch_one','Inspect the neighbourhood marker.',(10,1,12)),interact('witness_record','Read the witness report.',(12,1,12)),aid('local_aid','Assist the injured resident.',(12,1,36))]),
        ('evidence_trail','Investigation / evidence trail',[interact('clue_one','Inspect the first clue.',(12,1,12)),move('trail','Follow the service passage.',(42,1,24)),interact('clue_two','Recover the second clue.',(9,1,39))]),
        ('flood_contract','Rescue / flood contract',[move('water_entry','Enter the flood lane.',(10,1,26)),aid('stranded_resident','Stabilise the stranded resident.',(14,2,30)),escort('resident_exit','Guide the resident to the extraction pad.')]),
        ('community_support','Support / community shelter',[aid('volunteer','Assist the injured volunteer.',(12,1,36)),defend('community_shelter','Hold the community shelter.'),interact('supplies_record','Log the delivered supplies.',(9,1,39))]),
        ('bounty_contract','Bounty / restrained containment',[judgment('bounty_judgment','A bystander is caught near the exercise target. Choose your priority.'),aid('bounty_bystander','Protect the bystander first.',(12,1,36)),fight('bounty_target','Contain or disable the exercise target.')]),
        ('rooftop_search','Exploration / service-route search',[move('search_one','Search the northern service route.',(40,1,10)),move('search_two','Search beyond the collapsed-wall simulation.',(28,1,27)),interact('lost_record','Recover the missing field record.',(9,1,39)),escort('search_partner','Guide your search partner home.')]),
        ('mutual_aid','Mutual aid / coordinated response',[interact('response_plan','Read the coordinated response plan.'),aid('response_partner','Assist your response partner.',(12,1,36)),defend('response_shelter','Protect the response shelter.'),fight('response_sentinel','Resolve the field-sentinel encounter.',True)]),
        ('independent_operation','Independent / major operation',[judgment('operation_priority','Choose the safe priority for the major operation.'),escort('operation_evacuation','Lead the evacuation.'),aid('operation_aid','Recover the field partner.',(12,1,36)),fight('operation_boss','Resolve Bulwark using cover, positioning or emergency shutdown.',True),interact('independent_record','Submit the independent operation record.',(24,1,8))])]
    for i,(id,name,stages) in enumerate(independent):add(id,name,'independent',stages,150+i*25,3)
    path=ROOT/'src/main/resources/data/mha/mha/missions.json';path.write_text(json.dumps(rows,indent=2)+'\n',encoding='utf-8')
    print(f'Authored {len(rows)} missions: 10 school, 10 villain, 8 independent; {sum(len(m["stages"]) for m in rows)} objective stages. Runtime/visual verification remains separate.')

if __name__=='__main__':main()
