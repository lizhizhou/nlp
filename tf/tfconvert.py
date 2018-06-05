import tensorflow as tf

# This is your model definition.
xs = tf.placeholder(tf.float32, [None, 1])

W1 = tf.Variable(tf.zeros([1,10])+0.2)
b1 = tf.Variable(tf.zeros([10])+0.1)
Wx_plus_b1 = tf.nn.bias_add(tf.matmul(xs,W1), b1)
output = tf.nn.tanh(Wx_plus_b1, name="output")

# Adding the following lines right after your model definition
from bigdl.util.tf_utils import dump_model
dump_model_path = "/tmp/model"
# This line of code will create a Session and initialized all the Variable and
# save the model definition and variable to dump_model_path as BigDL readable format.
dump_model(path=dump_model_path)
