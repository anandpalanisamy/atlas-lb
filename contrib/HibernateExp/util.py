#!/usr/bin/env jython


import org.openstack.atlas.adapter.zxtm.ZxtmServiceStubs as ZxtmServiceStubs
import java.net.URL as URL

import org.openstack.atlas.util.crypto.CryptoUtil as CryptoUtil
import org.hexp.hibernateexp.util.BitUtil as BitUtil
import org.hexp.hibernateexp.util.BitUtil.BitOp as BitOp
import org.hexp.hibernateexp.util.HashUtil as HashUtil
import org.hexp.hibernateexp.util.HibernateUtil as HibernateUtil
import org.hexp.hibernateexp.HuApp as HuApp

import org.openstack.atlas.service.domain.entities.VirtualIp as VirtualIp
import org.openstack.atlas.service.domain.entities.VirtualIpv6 as VirtualIpv6
import org.openstack.atlas.service.domain.entities.VirtualIpType as VirtualIpType
import org.openstack.atlas.service.domain.entities.Cluster as Cluster
import org.openstack.atlas.service.domain.entities.DataCenter as DataCenter
import org.openstack.atlas.service.domain.entities.IpVersion as IpVersion
import org.openstack.atlas.service.domain.entities.LoadBalancer as LoadBalancer
import org.openstack.atlas.service.domain.entities.AccessList as AccessList
import org.openstack.atlas.service.domain.entities.AccessListType as AccessListType
import org.openstack.atlas.service.domain.entities.HealthMonitor as HealthMonitor
import org.openstack.atlas.service.domain.entities.HealthMonitorType as HealthMonitorType
import org.openstack.atlas.service.domain.entities.Node as Node
import org.openstack.atlas.service.domain.entities.NodeStatus as NodeStatus
import org.openstack.atlas.service.domain.entities.Host as Host
import org.openstack.atlas.service.domain.entities.Backup as Backup
import org.openstack.atlas.service.domain.entities.Usage as Usage
import org.openstack.atlas.service.domain.entities.HostStatus as HostStatus
import org.openstack.atlas.service.domain.entities.NodeCondition as NodeCondition
import org.openstack.atlas.service.domain.entities.NodeStatus as NodeStatus
import org.openstack.atlas.service.domain.entities.LoadBalancerJoinVip6 as LoadBalancerJoinVip6
import org.openstack.atlas.service.domain.entities.LoadBalancerJoinVip as LoadBalancerJoinVip
import org.openstack.atlas.service.domain.entities.ConnectionLimit as ConnectionLimit
import org.openstack.atlas.service.domain.entities.LoadBalancerAlgorithm as LBA
import org.openstack.atlas.service.domain.entities.LoadBalancerStatus as LBS
import org.openstack.atlas.service.domain.entities.LoadBalancerProtocol as LBP
import org.openstack.atlas.service.domain.entities.LoadBalancerProtocolObject as LoadBalancerProtocolObject
import org.openstack.atlas.service.domain.entities.SessionPersistence as SessionPersistence
import org.openstack.atlas.service.domain.entities.LoadBalancerAlgorithmObject as LoadBalancerAlgorithmObject
import org.openstack.atlas.service.domain.entities.Account as Account
import org.openstack.atlas.lb.helpers.ipstring.IPv4ToolSet as IPv4ToolSet
import org.openstack.atlas.lb.helpers.ipstring.exceptions.IPStringConversionException as IPStringConversionException
import org.openstack.atlas.lb.helpers.ipstring.IPv4Range as IPv4Range
import org.openstack.atlas.lb.helpers.ipstring.IPv4Ranges as IPv4Ranges
import org.openstack.atlas.service.domain.events.entities.Event as Event
import org.openstack.atlas.service.domain.events.entities.Alert as Alert
import org.openstack.atlas.service.domain.events.entities.AlertStatus as AlertStatus
import org.openstack.atlas.service.domain.events.entities.AccessListEvent as AccessListEvent
import org.openstack.atlas.service.domain.events.entities.ConnectionLimitEvent as ConnectionLimitEvent
import org.openstack.atlas.service.domain.events.entities.HealthMonitorEvent as HealthMonitorEvent
import org.openstack.atlas.service.domain.events.entities.LoadBalancerEvent as LoadBalancerEvent
import org.openstack.atlas.service.domain.events.entities.LoadBalancerServiceEvent as LoadBalancerServiceEvent
import org.openstack.atlas.service.domain.events.entities.NodeEvent as NodeEvent
import org.openstack.atlas.service.domain.events.entities.VirtualIpEvent as VirtualIpEvent
import org.openstack.atlas.service.domain.events.entities.SessionPersistenceEvent as SessionPersistenceEvent
import org.openstack.atlas.service.domain.entities.Entity as Entity
import org.openstack.atlas.service.domain.entities.JobState as State
import org.openstack.atlas.service.domain.entities.RateLimit as RateLimit
import org.openstack.atlas.service.domain.entities.AccountLimit as AccountLimit
import org.openstack.atlas.service.domain.entities.LimitType as LimitType
import org.openstack.atlas.service.domain.entities.UserPages as UserPages


import org.openstack.atlas.util.ip.IPUtils   as IPUtils
import org.openstack.atlas.util.ip.IPv6      as IPv6
import org.openstack.atlas.util.ip.IPv6Cidrs as IPv6Cidrs
import org.openstack.atlas.util.ip.IPv6Cidr  as IPv6Cidr
import org.openstack.atlas.util.ip.IPv4Cidrs as IPv4Cidrs
import org.openstack.atlas.util.ip.IPv4Cidr  as IPv4Cidr
import org.openstack.atlas.util.ip.IPv4      as IPv4

bigInteger2IPv6 = IPv6.bigInteger2IPv6

import java.math.BigInteger as BigInteger

import java.util.Date
import java.util.Calendar
import java.util.Set
import java.util.HashSet
import java.lang.Integer
import java.lang.String
import java.text.ParseException;
import java.text.SimpleDateFormat;

import org.openstack.atlas.service.domain.pojos.CustomQuery as CustomQuery
import org.openstack.atlas.service.domain.pojos.QueryParameter as QueryParameter
import org.openstack.atlas.util.converters.DateTimeConverters as DateTimeConverters

import java.util.ArrayList as ArrayList
import java.util.List as List
import org.openstack.atlas.docs.loadbalancers.api.management.v1.ListOfInts as ListOfInts

import time
import datetime
import simplejson as json
import java.util.Calendar as Calendar
import netcidr
import sys
import os
import string
import random
import re

import traceback

ubyte2int = BitUtil.ubyte2int
int2ubyte = BitUtil.int2ubyte
sha1sum = HashUtil.sha1sum
sha1sumHex = HashUtil.sha1sumHex

calToisoNoExc = DateTimeConverters.calToisoNoExc
calToiso = DateTimeConverters.calToiso

isoTocal = DateTimeConverters.isoTocal
isoTocalNoExc = DateTimeConverters.isoTocalNoExc

isValidIpv6Subnet = IPUtils.isValidIpv6Subnet
isValidIpv6String = IPUtils.isValidIpv6String
isValidIpv4Subnet = IPUtils.isValidIpv4Subnet
isValidIpv4String = IPUtils.isValidIpv4String

Date = java.util.Date
Calendar = java.util.Calendar
Set = java.util.Set
HashSet = java.util.HashSet
Integer = java.lang.Integer
String = java.lang.String
ipv4BlockToRange = IPv4ToolSet.ipv4BlockToRange
long2ip = IPv4ToolSet.long2ip
ip2long = IPv4ToolSet.ip2long
ParseException = java.text.ParseException
SimpleDateFormat = java.text.SimpleDateFormat

ipv4BlockToIpStrings = IPv4ToolSet.ipv4BlockToIpStrings
ipv4BlocksToIpStrings = IPv4ToolSet.ipv4BlocksToIpStrings

def printf(format,*args): sys.stdout.write(format%args)

n = netcidr.NetCidr()

LoadBalancerAlgorithm = LBA
LoadBalancerProtocol = LBP
LoadBalancerStatus = LBS
rnow = Calendar.getInstance()

rnd = random.Random()

weeks4 = 60*60*24*7*4

getter_re = re.compile("get.*|is.*",re.IGNORECASE)
setter_re = re.compile("set.*",re.IGNORECASE)

stubs = None

app = HuApp()

#select v.id,v.ip_address,lv.loadbalancer_id,l.account_id from virtual_ip_ipv4 v left join loadbalancer_virtualip lv on v.id = lv.virtualip_id join loadbalancer l on lv.loadbalancer_id = l.id order by v.id;

class ZxtmStubs(object):
    stubMap = {
               "ce":"getZxtmConfExtraBinding",
               "p" :"getPoolBinding",
               "pc":"getProtectionBinding",
               "tg":"getTrafficIpGroupBinding",
               "vs":"getVirtualServerBinding"
}

    def __init__(self,endpoints,user,passwd):
        self.user = user
        self.passwd = passwd
        self.endpoints = endpoints
        endpointkeys = endpoints.keys()
        self.endpoint = endpoints[endpointkeys[0]]
        endpoint = self.endpoint
        self.stubs = ZxtmServiceStubs.getServiceStubs(endpoint,user,passwd)

    def setEndpoint(self,id):
        self.endpoint = self.endpoints[id]
        endpoint = self.endpoint
        user = self.user
        passwd = self.passwd
        self.stubs = ZxtmServiceStubs.getServiceStubs(endpoint,user,passwd)
        
    def getMethods(self,stubName):
        out = []
        out = dir(self.__getattr__(stubName))
        out.sort()
        return out
        
        
    def __getattr__(self,stubName):
        if not ZxtmStubs.stubMap.has_key(stubName):
            raise AttributeError("'ZxtmStubs' has no attribute '%s'"%stubName)
        else:
            f = getattr(self.stubs,ZxtmStubs.stubMap[stubName])
            return f()


class CidrBlackList(object):
    def __init__(self):
        self.ipv4Cidrs = IPv4Cidrs()
        self.ipv6Cidrs = IPv6Cidrs()
        self.ipv4Cidrs.getCidrs()
        self.ipv6Cidrs.getCidrs()
        self.bad4Node = []
        self.bad6Node = []
        self.good4Node = []
        self.good6Node = []
        self.badNode = []
        self.goodNode = []

    def checkBlacklisted(self,node):
        ip = node[3]
        validipv4 = isValidIpv4String(ip)
        validipv6 = isValidIpv6String(ip)
        if not validipv4 and not validipv6:
            self.badNode.append(node)
            return
        if validipv4 and self.ipv4Cidrs.contains(ip):
            self.bad4Node.append(node)
            return	
        else:
            self.good4Node.append(node)
            return

        if validipv6 and self.ipv6Cidrs.contains(ip):
            self.bad6Node.append(node)
            return
        else:
            self.good6Node.append(node)
            return
        self.goodNode.append(node)
        return

    def addCidr(self,cidr):
        valid4sn = isValidIpv4Subnet(cidr)
        valid6sn = isValidIpv6Subnet(cidr)
        if not valid4sn and not valid6sn:
            return "SKIPPING %s not valid ipv4 or ipv6 subnet"%cidr
        if valid4sn:
            self.ipv4Cidrs.getCidrs().add(IPv4Cidr(cidr))
            return "ADD %s to ipv4subnets"%cidr
        if valid6sn:
            self.ipv6Cidrs.getCidrs().add(IPv6Cidr(cidr))
            return "ADD %s to ipv6subnets"%cidr
        return "UNKNOWN ERROR %s"%cidr

def bi(val):
    return BigInteger("%i"%val)

def chop(line):
    return line.replace("\r","").replace("\n","")

def choplines(lines):
    lines_out = []
    for line in lines:
        lines_out.append(chop(line))
    return lines_out
        
def pythonList(array_list):
    out = []
    for item in array_list:
        row = []
        for field in item:
            row.append(field)
        out.append(row)
    return out

def newAccounts(accountIds):
    out = []
    for account_id in accountIds:
        accountBytes = String("%s"%account_id).getBytes()
        sha1_hex = sha1sumHex(accountBytes,0,4)
        ac = Account()
        ac.setId(account_id)
        ac.setSha1SumForIpv6(sha1_hex)
        out.append(ac)
    return out

#Doesn't work
def linkLbs2Vips(lbs,vips):
    for i in xrange(0,len(vips)):
        for j in xrange(0,len(lbs)):
            vips[i].getLoadBalancers().add(lbs[j])


#Got tired of saying buildQuery(cq).list()
def bs(cq):
    return buildQuery(cq).list()


#Works
def linkVips2Lbs(lbs,vips):
    for i in xrange(0,len(lbs)):
        for j in xrange(0,len(vips)):
            lbs[i].getVirtualIps().add(vips[j])


def getlv(id):
    query  = "select v.loadBalancers from VirtualIp v "
    query += "where v.id = %s"
    return app.getList(query%id)

def getvl(id):
    query = "select l.virtualIps from LoadBalancer l where l.id = %s"
    return app.getList(query%id)

def getCluster(id):
    cls = app.getList("from Cluster where id = %s"%id)
    if len(cls) == 0:
        return None
    return cls[0]

def getEmptyVips():
    query  = "select v from VirtualIp v " 
    query += "    left join fetch v.loadBalancers l "
    query += "    group by v having count(l)=0 "
    return app.getList(query)

def begin(*args):
    if len(args)>=1:
        app.setDb(args[0])
    printf("useing db = \"%s\"\n",app.getDb())
    tx = app.getSession().beginTransaction()
    return tx;

def commit():
    app.getSession().getTransaction().commit()


def rollback():
    app.getSession().getTransaction().rollback()
	
def getSession():
    return app.getSession()

def istx():
    return app.getSession().isTransactionInProgress()

def close(s):
    app.getSession().close()

def setConfig(*args):
    global zxtmUser,zxtmPasswd,stubs

    if len(args)<1:
        file_name = "./local.json"
    else:
        file_name = args[0]
    config = load_json(file_name)
    dbConfigs = config["database"]
    default_db = dbConfigs[0]["db_key"]
    zxtm = config["zxtm"]
    zxtmUser = zxtm["user"]
    zxtmPasswd = CryptoUtil.decrypt(zxtm["passwd"])
    endpoints = {}
    for(k,v) in zxtm["endpoints"].items():
        ki = int(k)
        endpoints[ki] = URL(v)
    stubs = ZxtmStubs(endpoints,zxtmUser,zxtmPasswd)
    for dbConfig in dbConfigs:
        db_key = dbConfig["db_key"]
        url = dbConfig["url"]
        user = dbConfig["user"]
        passwd = CryptoUtil.decrypt(dbConfig["passwd"])
        hbm2ddl = dbConfig["hbm2ddl"]
        mapcfg = load_json(dbConfig["mapfile"])
        package = mapcfg["package"] if mapcfg.has_key("package") else None
        mapped = mapcfg["classes"]
        printf("adding %s\n",(db_key,url,user,passwd,hbm2ddl,package,mapped))
        app.setDbMap(db_key,url,user,passwd,hbm2ddl,package,mapped)
    app.setDb(default_db)


def getDate(dateStr):
    sdf = SimpleDateFormat("yyyy-MM-dd")
    date = sdf.parse(dateStr)
    cal = Calendar.getInstance()
    cal.setTime(date)
    return cal

def getDateTime(dateStr):
    sdf = SimpleDateFormat("yyyy-MM-dd")
    date = sdf.parse(dateStr)
    cal = Calendar.getInstance()
    cal.setTime(date)
    return cal

def stripPackageName(className):
    outList = []
    for i in xrange(len(className)-1,-1,-1):
        if className[i] == ".":
            break
        outList.append(className[i])
    outList.reverse()
    out = string.join(outList,"")
    return out

def generateImport(classList):
    out = ""
    for className in classList:
        localName = stripPackageName(className)
        out += "import %s as %s\n"%(className,localName)
    return out

def pad(digits,ch,val,**kargs):
  LEFT_DIR = 1
  RIGHT_DIR = 2
  str_out=str(val)
  if not "side" in kargs:
    kargs["side"]=LEFT_DIR
  if kargs["side"]==LEFT_DIR or kargs["side"]=="LEFT_DIR":
    for i in xrange(0,digits-len(str_out)):
      str_out = ch + str_out
    return str_out
  if kargs["side"]==RIGHT_DIR or kargs["side"]=="RIGHT_DIR":
    for i in xrange(0,digits-len(str_out)):
      str_out = str_out + ch
    return str_out


def txin(list_in):
    session = app.getSession()
    session.beginTransaction()
    for obj in list_in:
        session.update(obj)
    return session

def getters(obj):
    out = []
    for m in dir(obj):
        if getter_re.match(m):
            out.append(m)
    return out

def setters(obj):
    out = []
    for m in dir(obj):
        if setter_re.match(m):
            out.append(m)
    return out

def setobjattr(list_in,k,v):
    out = []
    for obj in list_in:
        attr = getattr(obj,k)
        attr(v)
        out.append(obj)
    return out

def setrndobjattr(list_in,k,choices):
    out = []
    for obj in list_in:
       attr = getattr(obj,k)
       attr(rnd.choice(choices))
       out.append(obj)
    return out

def saveList(list_in):
    failed_objs = 0
    session = app.getSession()
    for obj in list_in:
        try:
            session.save(obj)
        except:
            try:
                printf("rejected %s\nerror:%s\n",obj,traceback.format_exc())
            except:
                continue
            failed_objs += 1
    if failed_objs > 0:
        printf("Rejected %i objects during save\n",failed_objs)

def updateList(list_in):
    session = app.getSession()
    for obj in list_in:
        session.update(obj)

def mergeList(list_in):
    session = app.getSession()
    for obj in list_in:
        session.merge(obj)


def newObj(cls,**kw):
    obj = cls()
    for(k,v) in kw.items():
        attr = getattr(obj,"set"+k)
        attr(v)
    return obj

def rndClusters(num):
    out = []
    for i in xrange(0,num):
        rnum = rnd.randint(0,100000)
        cluster_name = "Cluster %i"%rnum   
        cluster_desc = "Description  %i"%rnum
        cluster = newObj(Cluster,Name=cluster_name,Description=cluster_desc)
        out.append(cluster)
    return out

def ri(x,y):
    return rnd.randint(x,y)

def rfloat(lo,hi):
    return rnd.uniform(lo,hi)

def randomIp():
    ip = "%i.%i.%i.%i"%(ri(0,255),ri(0,255),ri(0,255),ri(0,255))
    return ip

def rndNodes(num):
    out = []
    for i in xrange(0,num):
        n = Node()
        n.setIpAddress(randomIp())
        n.setCondition(rnd.choice(NodeCondition.values()))
        n.setPort(80)
        n.setStatus(rnd.choice(NodeStatus.values()))
        n.setWeight(ri(0,1000)) #lbs kgs???
        out.append(n)
    return out

def rndAccessLists(num):
    out = []
    for i in xrange(0,num):
        al = AccessList()
        al.setIpAddress(randomIp())
        al.setType(rndw.choice(AccessListType.values()))
        out.append(al)
    return out

def getCluster(id):
    return qq("SELECT c from Cluster c where id = %i"%id)[0]
    
def getlv6(vid):
    out = []
    qStr  = "select v.loadBalancer from LoadBalancerJoinVip6 "
    qStr += "v where v.virtualIp=%i"%vid
    lbs = qq(qStr)
    return lbs

def newVip6(cid,accountIdList,vipOctetList):
    out = []
    cluster = getCluster(cid)
    for aid in accountIdList:
        for vipOctet in vipOctetList:
            v = VirtualIpv6()
            v.setAccountId(aid)
            v.setCluster(cluster)
            v.setVipOctets(vipOctet)
            out.append(v)
    return out

def getVip6(aid,vo):
    qStr = "SELECT v from VirtualIpv6 v where accountId=%i and vipOctets=%i"
    v = qq(qStr%(aid,vo))
    if len(v) == 0:
        return None
    return v[0]

def getLoadBalancer(lid):
    lbs = qq("SELECT lb from LoadBalancer lb where id=%i"%lid)
    if len(lbs) == 0:
        return None
    return lbs[0]

def jlv6(lid,aid,vo):
    lb = getLoadBalancer(lid)
    v = getVip6(aid,vo)
    port = lb.getPort()
    j = LoadBalancerJoinVip6(port,lb,v)
    mergeList([j])
    
    return j

#Execute alter table host drop core_device_id
#execute alter table host add core_device_id varchar(64) not null        
def rndHosts(num):
    out = []
    for i in xrange(0,num):
        rnum = ri(0,10000)
        h = Host()
        h.setName("Host.%s"%rnum)
        h.setCoreDeviceId("%i"%ri(0,10000))
        h.setMaxConcurrentConnections(9)
        h.setHostStatus(rnd.choice(HostStatus.values()))
        h.setManagementIp(randomIp())
        c = app.getList("from Cluster")
        h.setCluster(rnd.choice(c))
        out.append(h)
    return out

def newConnectionLimit(lb):
    cl = ConnectionLimit()
    cl.setLoadBalancer(lb)
    cl.setMaxConnectionRateFromIp(32)
    cl.setMaxConnectionRateTimer(64)
    cl.setMaxConnectionsFromIp(5)
    cl.setMinConnections(3)
    return cl

def rndLoadBalancers(num):
    out = []
    today = Calendar.getInstance()
    hosts = app.getList("from Host")
    rps = app.getList("from LoadBalancerRateProfile")
    for i in xrange(0,num):
        rnum = ri(0,10000)
        lb = LoadBalancer()
        lb.setName("LB.%i"%rnum)
        lb.setAccountId(31337)
        lb.setPort(80)
        lb.setSessionPersistence(False)
        lb.setUpdated(today)
        lb.setCreated(today)
        lb.setConnectionLogging(False)
        lb.setAlgorithm(rnd.choice(LoadBalancerAlgorithm.values()))
        lb.setProtocol(rnd.choice(LoadBalancerProtocol.values()))
        lb.setSessionPersistence(False)
        lb.setStatus(rnd.choice(LoadBalancerStatus.values()))
        lb.setHost(rnd.choice(hosts))
        lb.setLoadBalancerRateProfile(rnd.choice(rps))
        out.append(lb)
    return out

def freeVips():
    vips = app.getList("from VirtualIp where locked != 1 and loadbalancer = null")
    ipInts = [n.strtoip(v.getIpAddress()) for v in vips]
    ipInts.sort()
    return [n.iptostr(ip) for ip in ipInts]

def allVips():
    vips = app.getList("from VirtualIp")
    ipInts = [n.strtoip(v.getIpAddress()) for v in vips]
    ipInts.sort()
    return [n.iptostr(ip) for ip in ipInts]


def rndVips(num,cl_types,vi_types):
    out = []
    for i in xrange(0,num):
        vip = VirtualIp()
        rnum = ri(0,10000)
        vip.setIpAddress(randomIp())
        vip.setIpVersion(IpVersion.IPV4)
        vip.setCluster(rnd.choice(cl_types))
        vip.setVipType(rnd.choice(vi_types))
        out.append(vip)
    return out

def rndRateProfile(num):
    out = []
    for i in xrange(0,num):
        rp = LoadBalancerRateProfile()
        rp.setConnectionThreshold(ri(0,1000))
        rp.setEnabled(True)
        rp.setName("RP.%i"%i)
        rp.setPublic(True)
        out.append(rp)
    return out

def newClusters(names,ds="DFW"):
    out = []
    for name in names:
        desc = "Cluster_name: %s"%name
        kw={}
        kw["Description"]=desc
        kw["Name"]=name
        kw["DataCenter"]=DataCenter.valueOf(ds)
        kw["Password"]="***"
        kw["Username"]="wtf"
        out.append(newObj(Cluster,**kw))
    return out  

def newHost(cluster):
    out = newObj(Host,Name="H1",Id=1,HostStatus=HostStatus.values()[0],
                 Cluster=cluster,CoreDeviceId="1",MaxConcurrentConnections=9,
                 ManagementIp="127.0.0.1")
    return out

def newRateProfile():
    out = newObj(LoadBalancerRateProfile,Public=True,Name="RP1",
                 Id=1,Enabled=True,ConnectionThreshold=100)
    return out
    

def newConnectionLimit(lbs):
    out = []
    for lb in lbs:
        if lb.getConnectionLimit() != None:
            continue
        cl = ConnectionLimit()
        cl.setLoadBalancer(lb)
        cl.setMaxConnectionRate(100)
        cl.setMaxConnections(200)
        cl.setMinConnections(300)
        cl.setRateInterval(60)
        out.append(cl)
    return out

def linkConnectionLimits2LoadBalancer(cls):
    for cl in cls:
        lb = cl.getLoadBalancer()
        lb.setConnectionLimit(cl)
        app.saveOrUpdate(lb)


def newLoadBalancers(accountId,num,hosts,rateprofile):
    today = Calendar.getInstance()
    out = []
    for i in xrange(0,num):
        host = rnd.choice(hosts)
        cluster = host.getCluster()
        lb = LoadBalancer()
        lb.setHost(host)
        lb.setName("LB.%i"%ri(0,10000))
        lb.setAccountId(accountId)
        lb.setConnectionLogging(rnd.choice([True,False]))
        lb.setAlgorithm(rnd.choice(LoadBalancerAlgorithm.values()))
        lb.setPort(80)
        lb.setUpdated(today)
        lb.setCreated(today)
        lb.setProtocol(rnd.choice(LoadBalancerProtocol.values()))
        lb.setSessionPersistence(rnd.choice(SessionPersistence.values()))
        lb.setStatus(rnd.choice(LoadBalancerStatus.values()))
        if not rateprofile:
            rps = app.getList("from LoadBalancerRateProfile")
            lb.setLoadBalancerRateProfile(rps[0])
        else:
            lb.setLoadBalancerRateProfile(rateprofile)
        out.append(lb)
    return out

def newVips(ip_block,cluster,vipType,rs=weeks4):
    out = []
    day = datetime.date.today()
    for ip in iprange(ip_block):
        la = rnddelta(rs)
        ld = rnddelta(rs)
        vip = VirtualIp()
        vip.setIpAddress(ip)
        vip.setVipType(VirtualIpType.valueOf(vipType))
        vip.setIpVersion(IpVersion.valueOf("IPV4"))
        vip.setCluster(cluster)
        vip.setLastAllocation(la)
        vip.setLastDeallocation(ld)
        out.append(vip)
    return out
#vips = newVips("172.16.0.0/20",c,"PUBLIC")

def newVipsLoHi(loIp,hiIp,cluster,vipType,rs=weeks4):
    out = []
    lo = n.strtoip(loIp)
    hi = n.strtoip(hiIp)
    ip = lo
    while ip <= hi:
        la = rnddelta(rs)
        ld = rnddelta(rs)
        vip = VirtualIp()
        vip.setIpAddress(n.iptostr(ip))
        vip.setVipType(VirtualIpType.valueOf(vipType))
        vip.setIpVersion(IpVersion.valueOf("IPV4"))
	vip.setCluster(cluster)
        vip.setLastAllocation(la)
        vip.setLastDeallocation(ld)
        out.append(vip)
        ip += 1
    return out
#vips = newVipsLoHi("174.12.11.7","174.12.11.128",c,"PUBLIC")
    

def newNodes(lbs,num):
    out = []
    for lb in lbs:
        for i in xrange(0,num):
            n = Node()
            n.setLoadbalancer(lb)
            n.setIpAddress(randomIp())
            n.setCondition(rnd.choice(NodeCondition.values()))
            n.setPort(80)
            n.setWeight(ri(0,1000))
            n.setStatus(rnd.choice(NodeStatus.values()))
            out.append(n)
    return out

def newAccessLists(lbs,num):
    out = []
    for lb in lbs:
        for i in xrange(0,num):
            a = AccessList()
            a.setIpAddress(randomIp())
            a.setIpVersion(IpVersion.IPV4)
            a.setType(rnd.choice(AccessListType.values()))
            a.setLoadbalancer(lb)
            out.append(a)
    return out

def rnddelta(maxsecs):
    dt = getcurrcal()
    r = rnd.uniform(0,maxsecs)
    s = int(r)
    ms = int((r-s)*1000)
    dt = timedelta(dt,s,Calendar.SECOND)
    dt = timedelta(dt,ms,Calendar.MILLISECOND)
    return dt

def timedelta(now,n,inc=Calendar.DAY_OF_MONTH):
    out = now.clone()
    out.add(inc,0 - n)
    return out

def getcurrcal():
    return Calendar.getInstance()

def newBackup(hosts,num,inc=Calendar.DAY_OF_MONTH):
    out = []
    now = getcurrcal()
    for host in hosts:
        for i in xrange(0,num):
            b = Backup()
            b.setHost(host)
            b.setName("BK-%i-%i-%i"%(host.getId(),i,ri(1000,9999)))
            b.setBackupTime(timedelta(now,i,inc=inc))
            out.append(b)
    return out

def newUsages(lbs,num,inc=Calendar.DAY_OF_MONTH):
    out = []
    now = getcurrcal()
    for lb in lbs:
        for i in xrange(0,num):
            u = Usage()
            u.setAverageConcurrentConnections(rfloat(0.0,1000.0))
            u.setIncomingTransfer(ri(0,1024*1024*1024*64))
            u.setOutgoingTransfer(ri(0,1024*1024*1024*64))
            u.setStartTime(timedelta(now,i-1,inc=inc))
            u.setEndTime(timedelta(now,i,inc=inc))
            u.setLoadbalancer(lb)
            u.setNumberOfPolls(100)
            u.setTags(0)
            u.setUserName("Your Mom")
            out.append(u)
    return out
#ul = newUsages(lbs,2000)

def newHealthMonitor(lbs,num):
    out = []
    pathformat = "/lb/%s/monitor/%s/blah/blah/blah"
    for lb in lbs:
        for i in xrange(0,num):
            h = HealthMonitor()
            h.setAttemptsBeforeDeactivation(ri(0,10))
            h.setBodyRegex(".*")
            h.setDelay(ri(0,600))
            h.setLoadbalancer(lb)
            h.setPath(pathformat%(lb.getId(),num))
            h.setStatusRegex(".*")
            h.setTimeout(ri(0,100))
            h.setType(rnd.choice(HealthMonitorType.values()))
            out.append(h)
    return out


def iprange(ip_block):
    out = []
    (netid,cidr) = n.strtoipblock(ip_block)
    for ip in n.ipset(netid,cidr):
        if ip&0xff == 0 or ip&0xff==0x00:
            continue
        out.append(n.iptostr(ip))
    list.sort(out)
    return out

def getabc(id=None):
    out = []
    query  = "select l.accountId ,c.id,count(*) from LoadBalancer l "
    query += "join l.host h "
    query += "join h.cluster c "
    if id:
        query += " where c.id = %s "%id
    query += "group by l.accountId, c.id"
    results = app.getList(query)
    for r in results:
        out.append((r[0],r[1],r[2]))
    return (out,query)

def getabh(id=None):
    out = []
    query  = "select l.accountId ,h.id,count(*) from LoadBalancer l "
    query += "join l.host h "
    if id:
        query += " where h.id = %s "%id
    query += "group by l.accountId, h.id"
    results = app.getList(query)
    for r in results:
        out.append((r[0],r[1],r[2]))
    return (out,query)


def getalb(accountId):
    out = []

    query = "select l.id, " \
          + "l.name, " \
          + "c.id, " \
          + "c.name, " \
          + "l.status, " \
          + "l.protocol " \
          + "from LoadBalancer l join l.host h join h.cluster c " \
          + "where l.accountId=%s";

    loadbalancers = app.getList(query%accountId)
    for l in loadbalancers:
        row = []
        for c in l:
            row.append(c)
        out.append(tuple(row))
    printf("\nQuery=\"%s\"\n",query)
    return out

def getrows(hrows):
    out = []
    for r in hrows:
        row = []
        for c in r:
            row.append(c)
        out.append(tuple(row))
    return out


def getcxhost(key):
    query  = "select l.accountId, l.id, "
    query += "l.name, n.id, n.ipAddress "
    query += "from LoadBalancer l left join l.nodes n "
    query += "join l.host h "
    out = []
    if type(key) == type(0):
        query += "where h.id = %i"%key
    elif type(key) == type(""):
        query += "where h.name = \"%s\" "
    else:
        return None
    query += " order by l.accountId, l.id, n.id "
    results = app.getList(query)
    for r in results:
        row = []
        for c in r:
            row.append(c)
        out.append(row)
    printf("\nQuery = %s\n",query)
    return out

def getcxcluster(key):
    query  = "select l.accountId, l.id, "
    query += "l.name, n.id, n.ipAddress "
    query += "from LoadBalancer l left join l.nodes n "
    query += "join l.host.cluster c "
    out = []
    if type(key) == type(0):
        query += "where c.id = %i"%key
    elif type(key) == type(""):
        query += "where c.name = \"%s\" "
    else:
        return None
    query += " order by l.accountId, l.id, n.id "
    results = app.getList(query)
    for r in results:
        row = []
        for c in r:
            row.append(c)
        out.append(row)
    printf("\nQuery = %s\n",query)
    return out

def printcx(cx):
    for x in cx:
        printf("%s\n",x)

def ipCounts():
    out = []
    query  = "select v.ipAddress,count(l) from VirtualIp v " 
    query += "left join v.loadBalancers l group by v "
    query += "order by count(l)"
    results = app.getList(query)
    for result in results:
        out.append((result[0],result[1]))
    return out

def nextVips():
    out = []
    query = "select v from VirtualIp v left join fetch v.loadBalancers l "
    query += "where locked=False group by v having count(l)=0 order "
    query += "by v.lastDeallocation asc "
    results = app.getList(query)
    for result in results:
        out.append((result.getIpAddress(),result.getId()))
    out.reverse()
    return out

def nullClusterVips():
    out = []
    query = "select v from VirtualIp v left join fetch v.loadBalancers l "
    query += "where locked=False group by v having count(l)=0 order " 
    query += "by v.lastDeallocation asc "
    results = app.getList(query)
    for result in results:
        if(result.getCluster() == None):
            out.append((result.getId(),result.getIpAddress()))
    out.reverse()
    return out

def qq(query):
    s = app.getSession()
    q = s.createQuery(query)
    return q.list()

def getbill(aid,ndo,inc=Calendar.DAY_OF_MONTH):
    s = app.getSession()
    out = []
    now = Calendar.getInstance()
    tdelta = timedelta(now,ndo,inc=inc)
    query  = "select u.loadbalancer.accountId, u.loadbalancer.id, "
    query += "u.loadbalancer.name, u.numVips, "
    query += "u.startTime, u.endTime, u.incomingTransfer, "
    query += "u.outgoingTransfer from Usage u "
    query += "where u.loadbalancer.accountId = :aid and startTime >= :tdelta "
    query += "order by u.loadbalancer.accountId asc,u.loadbalancer.id asc, " 
    query += "u.endTime desc"
    hq = s.createQuery(query)
    hq.setParameter("aid",aid)
    hq.setParameter("tdelta",tdelta)
    results = hq.list()
    printf("%s\n",query)
    for r in results:
        row = []
        for c in r:
            row.append(c)
        out.append(row)
    printf("\nQuery = %s\n",query)
    return out

def getAccountsByVip(vid):
    out = []
    query  = "select distinct(l.accountId) from VirtualIp v "
    query += "left join v.loadBalancers l where v.id=%s "
    query += "order by(l.accountId)"
    results = app.getList(query%vid)
    printf("%s\n",query)
    for r in results:
        out.append(r)
    return out

def getEPH(clusterId): #getEndPointHost
    s = app.getSession()
    qStr  = "from Host h where h.soapEndpointActive = 1 "
    qStr += "and h.hostStatus in ('ACTIVE_TARGET', 'FAILOVER') "
    qStr += "and h.cluster.id = :clusterId "
    qStr += "order by h.hostStatus desc, h.id asc";
    q = s.createQuery(qStr).setParameter("clusterId",clusterId)
    q.setMaxResults(1)
    resp = q.list()
    if(len(resp)==0):
        return None
    return resp

def getAllHosts():
    s = app.getSession()
    qStr = "from Host h where  h.hostStatus in ('ACTIVE_TARGET', 'FAILOVER')"
    q = s.createQuery(qStr)
    resp = q.list()
    return resp

def getEPHS(clusterId):
    hl = getEPH(clusterId)
    if len(hl)<1:
        return None
    return hl[0].getEndpoint()


def getHostsbyCluster(clusterId):
    s = app.getSession()
    qStr = "from Host h where h.cluster.id = :clusterId"
    q = s.createQuery(qStr).setParameter("clusterId",clusterId)
    resp = q.list()
    return resp

def getHoldingIps():
    out = []
    s = app.getSession()
    oneday = rnow.clone()
    oneday.add(Calendar.SECOND,0-7*24*60*60)
    query  = "select v.cluster.id,v.vipType,count(v) from VirtualIp v "
    query += "where size(v.loadBalancers) = 0 and "
    query += "    v.lastDeallocation >= :oneday "
    query += "group by v.vipType, v.cluster.id "
    query += "order by v.cluster.id "
    q = s.createQuery(query).setParameter("oneday",oneday)
    results = q.list()
    ccid = results[0][0]
    cluster = {"CLUSTER":ccid}
    for r in results:
        if ccid != r[0]:
            ccid = r[0]
            out.append(cluster)
            cluster = {"CLUSTER":ccid}
        cluster[str(r[1])]=r[2]
    out.append(cluster)
    return out        


def getClearIps():
    out = []
    s = app.getSession()
    oneday = rnow.clone()
    oneday.add(Calendar.SECOND,0-24*60*60)
    query  = "select v.cluster.id,v.vipType,count(v) from VirtualIp v "
    query += "where size(v.loadBalancers) = 0 and "
    query += "   (v.lastDeallocation < :oneday or "
    query += "    v.lastDeallocation is null) "
    query += "group by v.vipType, v.cluster.id "
    query += "order by v.cluster.id "
    q = s.createQuery(query).setParameter("oneday",oneday)
    results = q.list()
    ccid = results[0][0]
    cluster = {"CLUSTER":ccid}
    for r in results:
        if ccid != r[0]:
            ccid = r[0]
            out.append(cluster)
            cluster = {"CLUSTER":ccid}
        cluster[str(r[1])]=r[2]
    out.append(cluster)
    return out        

def getTotalIps():
    out = []
    s = app.getSession()
    oneday = rnow.clone()
    oneday.add(Calendar.SECOND,0-7*24*60*60)
    query  = "select v.cluster.id,v.vipType,count(v) from VirtualIp v "
    query += "group by v.vipType, v.cluster.id "
    query += "order by v.cluster.id "
    q = s.createQuery(query)
    results = q.list()
    ccid = results[0][0]
    cluster = {"CLUSTER":ccid}
    for r in results:
        if ccid != r[0]:
            ccid = r[0]
            out.append(cluster)
            cluster = {"CLUSTER":ccid}
        cluster[str(r[1])]=r[2]
    out.append(cluster)
    return out        

def allocatedToday():
    out = []
    s = app.getSession()
    oneday = rnow.clone()
    oneday.add(Calendar.SECOND,0-24*60*60)
    query  = "select v.cluster.id,v.vipType,count(v) from VirtualIp v "
    query += "where v.lastAllocation > :oneday "
    query += "group by v.vipType, v.cluster.id "
    query += "order by v.cluster.id "
    q = s.createQuery(query).setParameter("oneday",oneday)
    results = q.list()
    ccid = results[0][0]
    cluster = {"CLUSTER":ccid}
    for r in results:
        if ccid != r[0]:
            ccid = r[0]
            out.append(cluster)
            cluster = {"CLUSTER":ccid}
        cluster[str(r[1])]=r[2]
    out.append(cluster)
    return out        

def allocatedThisWeek():
    out = []
    s = app.getSession()
    oneday = rnow.clone()
    oneday.add(Calendar.SECOND,0-7*24*60*60)
    query  = "select v.cluster.id,v.vipType,count(v) from VirtualIp v "
    query += "where v.lastAllocation > :oneday "
    query += "group by v.vipType, v.cluster.id "
    query += "order by v.cluster.id "
    q = s.createQuery(query).setParameter("oneday",oneday)
    results = q.list()
    ccid = results[0][0]
    cluster = {"CLUSTER":ccid}
    for r in results:
        if ccid != r[0]:
            ccid = r[0]
            out.append(cluster)
            cluster = {"CLUSTER":ccid}
        cluster[str(r[1])]=r[2]
    out.append(cluster)
    return out        

def load_json(json_file):
    full_path = os.path.expanduser(json_file)
    full_path = os.path.abspath(full_path)
    fp = open(full_path,"r")
    json_data = fp.read()
    fp.close()
    out = json.loads(json_data)
    return out

def save_json(json_file,obj):
    full_path = os.path.expanduser(json_file)
    full_path = os.path.abspath(full_path)
    fp = open(full_path,"w")
    out = json.dumps(obj, indent=2)
    fp.write(out)
    fp.close()

def getUsedIps():
    out = []
    s = app.getSession()
    query  = "select v.cluster.id,v.vipType,count(v) from VirtualIp v "
    query += "where size(v.loadBalancers) > 0 "
    query += "group by v.vipType, v.cluster.id "
    query += "order by v.cluster.id "
    q = s.createQuery(query)
    results = q.list()
    ccid = results[0][0]
    cluster = {"CLUSTER":ccid}
    for r in results:
        if ccid != r[0]:
            ccid = r[0]
            out.append(cluster)
            cluster = {"CLUSTER":ccid}
        cluster[str(r[1])]=r[2]
    out.append(cluster)
    return out        

def getRE(accountId,startDate=None,endDate=None):
    keys = []
    tMap = {}
    out = []
    s = app.getSession()
    qHead  = "select l from LoadBalancerServiceEvent l "
    qHead += "where  l.accountId = :accountId "
    qMid = ""
    if(startDate):
        qMid += " and created >= :startDate "
    if(endDate):
        qMid += " and created <= :endDate "
    qTail = "order by created "
    qStr = qHead + qMid + qTail
    q = s.createQuery(qHead + qMid + qTail)
    q.setParameter("accountId",accountId)
    if(startDate):
        q.setParameter("startDate",getDate(startDate))
    if(endDate):
        q.setParameter("endDate",getDate(endDate))
    results = q.list()
    for row in results:
        row_out = {}
        key = row.getLoadbalancerId()
        if not tMap.has_key(key):
            tMap[key]=[]
            keys.append(key)
        tMap[key].append(row.toString())
    keys.sort()
    for k in keys:
        rows = []
        for row in tMap[k]:
            rows.append(row)
        out.append({"lid":k,"entries":rows})
    return out

#printRE(getRE(354394))

def buildQuery(cq):
    s = app.getSession()
    qStr = cq.getQueryString()
    pList = cq.getQueryParameters()
    printf("\nQuery = %s\n\n",qStr)
    q = s.createQuery(qStr)
    for(pname,val) in [(p.getPname(),p.getValue()) for p in pList]:
        q.setParameter(pname,val)
    if cq.getLimit() != None:
        q.setMaxResults(cq.getLimit())
    if cq.getOffset() != None:
        q.setFirstResult(cq.getOffset)
    return q
        

def getLbsByipAddr():
    s = app.getSession()
    qStr  = "select v.ipAddress,v.id,l.id,l.accountId,l.port, "
    qStr += "v.ipVersion, v.vipType "
    qStr += "from VirtualIp v left join v.loadBalancers l "
    qStr += "order by l.accountId,l.id,l.port,v.id"
    results = s.createQuery(qStr).list()
    printf("%s\n",qStr)
    return getrows(results)

def getPortsByVip(vid):
    query  = "select v.id, l.id, l.accountId,l.port "
    query += "from VirtualIp v left join v.loadBalancers l "
    query += "where v.id = %s order by port,l.id"
    results = app.getList(query%vid).list()
    printf("%s\n",query)
    return getrows(results)


def getLbIsoDates(dataIn):
    out = []
    if dataIn.class == CustomQuery("").class:
        results = buildQuery(dataIn).list()
    else:
        results = dataIn
    for lb in results:
        out.append((lb.getId(),calToisoNoExc(lb.getUpdated())))
    return out

def printRE(re_in):
    for row in re_in:
        printf("%s:\n",row["lid"])
        for entry in row["entries"]:
            printf("    %s\n",entry)

def newAlert(account,lid,day):
    a = Alert()
    a.setAccountId(account)
    a.setAlertType("ZEUS_FAILURE")
    a.setCreated(getDate(day))
    a.setLoadbalancerId(lid)
    a.setMessage("TESTING")
    a.setMessageName("TESTING")
    a.setStatus(AlertStatus.valueOf("UNACKNOWLEDGED"))
    return a
